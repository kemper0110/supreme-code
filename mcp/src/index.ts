import {McpServer} from "@modelcontextprotocol/sdk/server/mcp";
import {StreamableHTTPServerTransport} from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import {CallToolResult} from "@modelcontextprotocol/sdk/types.js";
import http from "node:http";
import {setTimeout} from "node:timers/promises";

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
        "importThisProblem",
        {
            description: "Import a problem into Supreme Code platform",
        },
        async (): Promise<CallToolResult> => {
            await setTimeout(1000);
            return {content: [{type: "text", text: "importThisProblem done"}]};
        }
    );

    server.registerTool(
        "exportThisProblem",
        {
            description: "Export current problem from Supreme Code platform",
        },
        async (): Promise<CallToolResult> => {
            await setTimeout(1000);
            return {content: [{type: "text", text: "exportThisProblem done"}]};
        }
    );

    server.registerTool(
        "platform-info",
        {
            description: "Get information about the Supreme Code platform",
        },
        async (): Promise<CallToolResult> => {
            await setTimeout(100);
            const config = {
                tags: [
                    "math", "hashmap"
                ],
                languages: [
                    "cpp", "java", "javascript"
                ]
            }
            return {
                content: [
                    {type: "text", text: JSON.stringify(config)},
                ],
                structuredContent: config
            };
        }
    );

    server.registerTool(
        "validate",
        {
            description: "Validate current problem or solution",
        },
        async (): Promise<CallToolResult> => {
            await setTimeout(1000);
            return {content: [{type: "text", text: "Validated OK"}]};
        }
    );
    return server
}


const httpServer = http.createServer(async (req: http.IncomingMessage, res: http.ServerResponse) => {
    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }
    try {
        const server = createServer()
        const transport = new StreamableHTTPServerTransport();
        await server.connect(transport);
        await transport.handleRequest(req, res);
    } catch (e) {
        console.error('Error handling request:', e);
        if (!res.headersSent) {
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: 'Internal Server Error'}));
        }
    }
});

const port = 3200;

httpServer.listen(port, () => {
    console.log(`Supreme Code MCP server running on http://localhost:${port}`);
});
