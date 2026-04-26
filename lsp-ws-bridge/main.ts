import express from "express";
import {IncomingMessage, Server} from "node:http";
import {WebSocketServer} from "ws";
import {Socket} from "node:net";
import {dirname} from "node:path";
import {fileURLToPath, URL} from "node:url";
import {type IWebSocket, WebSocketMessageReader, WebSocketMessageWriter} from "vscode-ws-jsonrpc";
import {createConnection, createServerProcess} from "vscode-ws-jsonrpc/server";
import {Message} from "vscode-languageserver-protocol";
import {z} from "zod";

const LanguageServerRunConfigSchema = z.object({
    serverName: z.string(),
    pathName: z.string(),
    serverPort: z.coerce.number(),
    runCommand: z.string(),
    runCommandArgs: z.string().optional()
        .transform(val => val?.trim() ? val.trim().split(/\s+/) : [])
        .pipe(z.array(z.string())),
    runCommandCwd: z.string().optional(),
})

type LanguageServerRunConfig = z.infer<typeof LanguageServerRunConfigSchema>;

/** LSP server runner */
export function runLanguageServer(
    languageServerRunConfig: LanguageServerRunConfig,
) {
    process.on("uncaughtException", (err: Error) => {
        console.error(
            `Uncaught Exception: cause: ${
                JSON.stringify(err.cause ?? "unknown")
            } message: ${err.message}`,
        );
        if (err.stack !== undefined) {
            console.error(err.stack);
        }
    });

    // create the express application
    const app = express();
    // server the static content, i.e. index.html
    const dir = getLocalDirectory(import.meta.url);
    app.use(express.static(dir));
    // start the http server
    const httpServer: Server = app.listen(languageServerRunConfig.serverPort);
    const wss = new WebSocketServer({
        noServer: true,
        perMessageDeflate: false,
    });
    // create the web socket
    upgradeWsServer(languageServerRunConfig, {
        server: httpServer,
        wss,
    });
    console.log("Language server started");
}


export const launchLanguageServer = (
    runconfig: LanguageServerRunConfig,
    socket: IWebSocket,
) => {
    const {serverName, runCommand, runCommandArgs, runCommandCwd} = runconfig;
    // start the language server as an external process
    const reader = new WebSocketMessageReader(socket);
    const writer = new WebSocketMessageWriter(socket);
    const socketConnection = createConnection(
        reader,
        writer,
        () => socket.dispose(),
    );
    try {
        const serverConnection = createServerProcess(
            serverName,
            runCommand,
            runCommandArgs,
            runCommandCwd ? {
                cwd: runCommandCwd
            } : undefined,
        );
        if (serverConnection !== undefined) {
            socketConnection.forward(serverConnection, (message) => {
                // socket to server
                // onNotification toProcUri
                if (Message.isNotification(message)) {
                    console.log("notification client->server");
                    console.log(message);
                    return message;
                }
                // onRequest toProcUri
                if (Message.isRequest(message)) {
                    console.log("request client->server");
                    console.log(message);
                    return message;
                }
                // onRequest response fromProcUri
                if (Message.isResponse(message)) {
                    console.log("response client->server");
                    console.log(message);
                    return message;
                }
                return message;
            });
            serverConnection.forward(socketConnection, (message) => {
                // server to socket
                // onNotification fromProcUri
                if (Message.isNotification(message)) {
                    console.log("notification server->client");
                    console.log(message);
                    return message;
                }
                // onRequest fromProcUri
                if (Message.isRequest(message)) {
                    console.log("request server->client");
                    console.log(message);
                    return message;
                }
                if (Message.isResponse(message)) {
                    console.log("response server->client");
                    console.log(message);
                    return message;
                }

                return message;
            });
            socketConnection.onClose(() => serverConnection.dispose());
            serverConnection.onClose(() => socketConnection.dispose());
        }
    } catch (e) {
        console.error(e);
    }
};

function upgradeWsServer(
    runconfig: LanguageServerRunConfig,
    config: {
        server: Server;
        wss: WebSocketServer;
    },
) {
    config.server.on(
        "upgrade",
        (request: IncomingMessage, socket: Socket, head: Buffer) => {
            const baseURL = `http://${request.headers.host}/`;
            const pathName = request.url !== undefined
                ? new URL(request.url, baseURL).pathname
                : undefined;
            if (pathName === runconfig.pathName) {
                config.wss.handleUpgrade(request, socket, head, (webSocket) => {
                    const socket: IWebSocket = {
                        send: (content) =>
                            webSocket.send(content, (error: Error | null | undefined) => {
                                if (error !== null && error !== undefined) {
                                    throw error;
                                }
                            }),
                        onMessage: (cb) =>
                            webSocket.on("message", (data) => {
                                cb(data);
                            }),
                        onError: (cb) => webSocket.on("error", cb),
                        onClose: (cb) => webSocket.on("close", cb),
                        dispose: () => webSocket.close(),
                    };
                    // launch the server when the web socket is opened
                    if (webSocket.readyState === webSocket.OPEN) {
                        launchLanguageServer(runconfig, socket);
                    } else {
                        webSocket.on("open", () => {
                            launchLanguageServer(runconfig, socket);
                        });
                    }
                });
            }
        },
    );
}

/**
 * Solves: __dirname is not defined in ES module scope
 */
function getLocalDirectory(referenceUrl: string | URL) {
    const __filename = fileURLToPath(referenceUrl);
    return dirname(__filename);
}


runLanguageServer(LanguageServerRunConfigSchema.parse(process.env));