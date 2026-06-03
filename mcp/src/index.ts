import {McpServer} from "@modelcontextprotocol/sdk/server/mcp.js";
import {StreamableHTTPServerTransport} from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import {InMemoryEventStore} from "@modelcontextprotocol/sdk/examples/shared/inMemoryEventStore.js";
import type {CallToolResult} from "@modelcontextprotocol/sdk/types.js";
import {isInitializeRequest} from "@modelcontextprotocol/sdk/types.js";
import crypto from "node:crypto";
import http from "node:http";
import WebSocket, {WebSocketServer} from "ws";

type FrontendMethod =
    | "exportCurrentProblem"
    | "importCurrentProblem"
    | "platformInfo"
    | "validateCurrentProblem";

type FrontendRequest = {
    id: string;
    method: FrontendMethod;
    params?: Record<string, unknown>;
};

type FrontendResponse = {
    id: string;
    result?: unknown;
    error?: string;
};

type McpSession = {
    server: McpServer;
    transport: StreamableHTTPServerTransport;
};

const port = 3300;
const requestTimeoutMs = 30_000;

let frontendSocket: WebSocket | null = null;
const mcpSessions = new Map<string, McpSession>();
const pendingFrontendRequests = new Map<string, {
    resolve: (value: unknown) => void;
    reject: (reason: Error) => void;
    timeout: NodeJS.Timeout;
}>();

function jsonContent(value: unknown): CallToolResult {
    const structuredContent =
        value && typeof value === "object" && !Array.isArray(value)
            ? value as Record<string, unknown>
            : {value};

    return {
        content: [{type: "text", text: JSON.stringify(value, null, 2)}],
        structuredContent,
    };
}

function frontendConnected() {
    return Boolean(frontendSocket && frontendSocket.readyState === WebSocket.OPEN);
}

function sendCors(res: http.ServerResponse) {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, MCP-Protocol-Version, MCP-Session-Id, Last-Event-ID");
    res.setHeader("Access-Control-Expose-Headers", "MCP-Session-Id, MCP-Protocol-Version");
}

function sendJson(res: http.ServerResponse, status: number, body: unknown) {
    sendCors(res);
    res.writeHead(status, {"Content-Type": "application/json"});
    res.end(JSON.stringify(body));
}

function sendText(res: http.ServerResponse, status: number, text: string, headers: Record<string, string> = {}) {
    sendCors(res);
    res.writeHead(status, {"Content-Type": "text/plain; charset=utf-8", ...headers});
    res.end(text);
}

function sendJsonRpcError(res: http.ServerResponse, status: number, code: number, message: string) {
    sendJson(res, status, {
        jsonrpc: "2.0",
        error: {code, message},
        id: null,
    });
}

function requestHeader(req: http.IncomingMessage, name: string) {
    const header = req.headers[name.toLowerCase()]
    return Array.isArray(header) ? header[0] : header;
}

async function readJsonBody(req: http.IncomingMessage) {
    const chunks: Buffer[] = [];
    for await (const chunk of req) {
        chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
    }

    const rawBody = Buffer.concat(chunks).toString("utf8").trim();
    return rawBody ? JSON.parse(rawBody) : undefined;
}

function sendFrontendRequest(method: FrontendMethod, params?: Record<string, unknown>) {
    if (!frontendConnected()) {
        throw new Error("Supreme Code problem editor is not connected. Open the edit form first.");
    }

    const id = crypto.randomUUID();
    const request: FrontendRequest = {id, method, params};

    return new Promise<unknown>((resolve, reject) => {
        const timeout = setTimeout(() => {
            pendingFrontendRequests.delete(id);
            reject(new Error(`Frontend did not respond to ${method}`));
        }, requestTimeoutMs);

        pendingFrontendRequests.set(id, {resolve, reject, timeout});

        try {
            frontendSocket!.send(JSON.stringify(request));
        } catch (error) {
            clearTimeout(timeout);
            pendingFrontendRequests.delete(id);
            reject(error instanceof Error ? error : new Error("Failed to send request to frontend"));
        }
    });
}

function handleFrontendResponse(message: FrontendResponse) {
    const pending = pendingFrontendRequests.get(message.id);
    if (!pending) {
        return;
    }

    clearTimeout(pending.timeout);
    pendingFrontendRequests.delete(message.id);

    if (message.error) {
        pending.reject(new Error(message.error));
    } else {
        pending.resolve(message.result);
    }
}

function closeFrontendConnection(socket: WebSocket) {
    if (frontendSocket !== socket) {
        return;
    }

    for (const [id, pending] of pendingFrontendRequests) {
        clearTimeout(pending.timeout);
        pending.reject(new Error("Frontend disconnected"));
        pendingFrontendRequests.delete(id);
    }

    frontendSocket = null;
}

function createServer() {
    const server = new McpServer(
        {
            name: "supreme-code-mcp",
            version: "1.0.0",
        },
        {
            capabilities: {
                tools: {},
            },
        }
    );

    server.registerTool(
        "exportThisProblem",
        {
            description: "Ask the open Supreme Code editor form to export the current problem to its connected directory",
        },
        async (): Promise<CallToolResult> => jsonContent(await sendFrontendRequest("exportCurrentProblem"))
    );

    server.registerTool(
        "importThisProblem",
        {
            description: "Ask the open Supreme Code editor form to import the problem from its connected directory",
        },
        async (): Promise<CallToolResult> => jsonContent(await sendFrontendRequest("importCurrentProblem"))
    );

    server.registerTool(
        "platform-info",
        {
            description: "Get current Supreme Code tags and languages from the open authorized editor form",
        },
        async (): Promise<CallToolResult> => jsonContent(await sendFrontendRequest("platformInfo"))
    );

    server.registerTool(
        "validate",
        {
            description: "Validate the current problem through the open Supreme Code editor form",
        },
        async (): Promise<CallToolResult> => jsonContent(await sendFrontendRequest("validateCurrentProblem"))
    );

    return server;
}

function createStatefulMcpSession() {
    const server = createServer();
    const eventStore = new InMemoryEventStore();
    const transport = new StreamableHTTPServerTransport({
        sessionIdGenerator: () => crypto.randomUUID(),
        eventStore,
        onsessioninitialized: sessionId => {
            console.log(`MCP session initialized: ${sessionId}`);
            mcpSessions.set(sessionId, {server, transport});
        },
        onsessionclosed: sessionId => {
            console.log(`MCP session closed: ${sessionId}`);
            mcpSessions.delete(sessionId);
        },
    });

    transport.onclose = () => {
        const sessionId = transport.sessionId;
        if (sessionId) {
            mcpSessions.delete(sessionId);
        }
    };
    transport.onerror = error => {
        console.error("MCP transport error:", error);
    };

    return {server, transport};
}

async function handleMcpPost(req: http.IncomingMessage, res: http.ServerResponse) {
    let body: unknown;
    try {
        body = await readJsonBody(req);
    } catch (error) {
        console.error("Failed to parse MCP request body:", error);
        sendJsonRpcError(res, 400, -32700, "Parse error");
        return;
    }

    const sessionId = requestHeader(req, "mcp-session-id");
    if (sessionId) {
        const session = mcpSessions.get(sessionId);
        if (!session) {
            sendJsonRpcError(res, 404, -32001, "Session not found");
            return;
        }

        await session.transport.handleRequest(req, res, body);
        return;
    }

    if (!isInitializeRequest(body)) {
        sendJsonRpcError(res, 400, -32000, "Bad Request: No valid session ID provided");
        return;
    }

    const {server, transport} = createStatefulMcpSession();
    await server.connect(transport);
    await transport.handleRequest(req, res, body);
}

async function handleMcpGet(req: http.IncomingMessage, res: http.ServerResponse) {
    const sessionId = requestHeader(req, "mcp-session-id");
    const session = sessionId ? mcpSessions.get(sessionId) : undefined;
    if (!sessionId || !session) {
        sendText(res, 400, "Invalid or missing session ID");
        return;
    }

    await session.transport.handleRequest(req, res);
}

async function handleMcpDelete(req: http.IncomingMessage, res: http.ServerResponse) {
    const sessionId = requestHeader(req, "mcp-session-id");
    const session = sessionId ? mcpSessions.get(sessionId) : undefined;
    if (!sessionId || !session) {
        sendText(res, 400, "Invalid or missing session ID");
        return;
    }

    await session.transport.handleRequest(req, res);
}

async function handleMcpRequest(req: http.IncomingMessage, res: http.ServerResponse) {
    if (req.method === "POST") {
        await handleMcpPost(req, res);
        return;
    }

    if (req.method === "GET") {
        await handleMcpGet(req, res);
        return;
    }

    if (req.method === "DELETE") {
        await handleMcpDelete(req, res);
        return;
    }

    sendText(res, 405, "Method Not Allowed", {Allow: "GET,POST,DELETE,OPTIONS"});
}

const httpServer = http.createServer(async (req: http.IncomingMessage, res: http.ServerResponse) => {
    sendCors(res);
    if (req.method === "OPTIONS") {
        res.writeHead(200);
        res.end();
        return;
    }

    try {
        const url = new URL(req.url ?? "/", `http://${req.headers.host ?? `localhost:${port}`}`);
        if (url.pathname === "/api/status" && req.method === "GET") {
            sendJson(res, 200, {
                frontendConnected: frontendConnected(),
                pendingFrontendRequests: pendingFrontendRequests.size,
            });
            return;
        }

        if (url.pathname !== "/" && url.pathname !== "/mcp") {
            sendJson(res, 404, {error: "Not Found"});
            return;
        }

        await handleMcpRequest(req, res);
    } catch (error) {
        console.error("Error handling request:", error);
        if (!res.headersSent) {
            sendJson(res, 500, {error: "Internal Server Error"});
        }
    }
});

const frontendWebSocketServer = new WebSocketServer({
    server: httpServer,
    path: "/frontend",
});

frontendWebSocketServer.on("connection", socket => {
    if (frontendSocket && frontendSocket.readyState === WebSocket.OPEN) {
        frontendSocket.close(1000, "New Supreme Code editor connected");
    }

    frontendSocket = socket;
    socket.on("message", data => {
        try {
            const message = JSON.parse(data.toString("utf8")) as FrontendResponse;
            handleFrontendResponse(message);
        } catch (error) {
            console.error("Frontend WebSocket message error:", error);
            socket.close(1011, "Invalid frontend message");
        }
    });
    socket.on("close", () => closeFrontendConnection(socket));
    socket.on("error", () => closeFrontendConnection(socket));
});

httpServer.listen(port, () => {
    console.log(`Supreme Code MCP server running on http://localhost:${port}`);
    console.log(`Supreme Code frontend WebSocket waiting on ws://localhost:${port}/frontend`);
});

async function closeMcpSessions() {
    for (const [sessionId, session] of mcpSessions) {
        try {
            await session.transport.close();
        } catch (error) {
            console.error(`Failed to close MCP session ${sessionId}:`, error);
        } finally {
            mcpSessions.delete(sessionId);
        }
    }
}

async function shutdown(signal: NodeJS.Signals) {
    console.log(`Received ${signal}, shutting down Supreme Code MCP server`);
    await closeMcpSessions();
    frontendWebSocketServer.close();
    httpServer.close(() => process.exit(0));
}

process.on("SIGINT", signal => {
    void shutdown(signal);
});

process.on("SIGTERM", signal => {
    void shutdown(signal);
});
