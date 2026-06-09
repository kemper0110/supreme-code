import {WebSocketServer} from "ws";

import {env} from "./env.js";
import {z} from "zod";
import {Message} from "vscode-languageserver-protocol";
import Docker, {type Container} from 'dockerode';
import * as fs from "node:fs";
import * as stream from "node:stream";
import {PassThrough} from "node:stream";
import {pipeline} from "node:stream/promises";
import {type IWebSocket, WebSocketMessageReader, WebSocketMessageWriter} from "vscode-ws-jsonrpc";
import {createConnection, createStreamConnection} from "vscode-ws-jsonrpc/server";
import * as http from "node:http";
import {IncomingMessage} from "node:http";
import {Socket} from "node:net";
import {URL} from "node:url";
import { parse } from 'yaml'

const docker = new Docker();

const ConfigSchema = z.object({
    languages: z.record(z.string(), z.object({
        lspConfig: z.object({
            image: z.string(),
            cmd: z.array(z.string()).optional()
        }).optional()
    }))
})

const config = ConfigSchema.parse(parse(
    fs.readFileSync('../platform.yaml', 'utf8')
))

const querySchema = z.object({
    language: z.string(),
    mode: z.enum(['run', 'test']).default('run'),
})

type LspMode = z.infer<typeof querySchema>['mode']

const workDirByMode: Record<LspMode, string> = {
    run: '/usr/run',
    test: '/usr/test',
}

class LspConnectionError extends Error {
    constructor(message: string, readonly closeCode: number) {
        super(message);
    }
}

function closeReason(message: string) {
    return Buffer.byteLength(message) <= 123 ? message : `${message.slice(0, 120)}...`;
}

function toCloseError(error: unknown): LspConnectionError {
    if (error instanceof LspConnectionError) {
        return error;
    }
    if (error instanceof z.ZodError) {
        return new LspConnectionError('Invalid LSP connection query', 1008);
    }
    return new LspConnectionError('Failed to start language server', 1011);
}

async function waitForContainerReady(container: Docker.Container): Promise<void> {
    while (true) {
        const inspect = await container.inspect();
        if (inspect.State.Running) {
            return;
        }
        // If the container exited unexpectedly, throw an error
        if (!inspect.State.Running && inspect.State.Status === 'exited') {
            throw new Error(`Container exited with code ${inspect.State.ExitCode}`);
        }
        await new Promise(resolve => setTimeout(resolve, 100));
    }
}

async function clearLSP(container: Container) {
    console.log('killing ', container.id)
    await container.kill().catch(() => {
    });
    await container.remove({force: true});
}

type LSP = {
    container: Container
    stdin: stream.Writable
    stdout: stream.Readable
}

async function createLSP(language: string, mode: LspMode): Promise<LSP> {
    const languageConfig = config.languages[language]
    if (!languageConfig)
        throw new LspConnectionError(`Language '${language}' not found`, 1008)
    if (!languageConfig.lspConfig)
        throw new LspConnectionError(`Language '${language}' has no LSP config`, 1008)

    const container = await docker.createContainer({
        Image: languageConfig.lspConfig.image,
        Cmd: languageConfig.lspConfig.cmd,
        Tty: false,
        AttachStdout: true,
        AttachStdin: true,
        AttachStderr: true,
        OpenStdin: true,
        StdinOnce: false,
        WorkingDir: workDirByMode[mode],
    });

    const attach = await container.attach({
        stream: true,
        stdin: true,
        stdout: true,
        stderr: true,
        hijack: true,
    });
    await container.start();

    const stdoutSplit = new PassThrough();
    const stderrSplit = new PassThrough();
    container.modem.demuxStream(attach, stdoutSplit, stderrSplit);

    await waitForContainerReady(container)

    const stdin = new PassThrough()
    pipeline(stdin, async function* (source) {
        for await (const chunk of source) {
            console.log('stdin chunk', chunk.length)
            yield chunk
        }
    }, attach).catch((e: Error) => {
        console.log('stdin pipeline error', e)
    })

    const stdout = new PassThrough()
    pipeline(stdoutSplit, async function* (source) {
        for await (const chunk of source) {
            console.log('stdout chunk', chunk.length)
            yield chunk
        }
    }, stdout).catch((e: Error) => {
        console.log('stdout pipeline error', e)
    })

    return {
        container,
        stdout,
        stdin,
    };
}

const server = http.createServer((req, res) => {
    res.write("hello world")
    res.end()
})
server.listen(env.PORT, () => {
    console.log('listening', server.address())
})
const wss = new WebSocketServer({
    noServer: true,
    perMessageDeflate: false,
});

function launchLanguageServer(socket: IWebSocket, lsp: LSP) {
    const reader = new WebSocketMessageReader(socket);
    const writer = new WebSocketMessageWriter(socket);
    const socketConnection = createConnection(
        reader,
        writer,
        () => socket.dispose(),
    );
    const serverConnection = createStreamConnection(lsp.stdout, lsp.stdin, () => socket.dispose());

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
    socketConnection.onClose(() => {
        console.log('socket dispose')
        return serverConnection.dispose();
    });
    serverConnection.onClose(() => {
        console.log('server dispose')
        return socketConnection.dispose();
    });
}

server.on('upgrade', async (request: IncomingMessage, socket: Socket, head: Buffer) => {
    let lsp: LSP;
    try {
        const url = new URL(request.url!, `http://${request.headers.host}/`);
        const query = querySchema.parse({
            language: url.searchParams.get('language') ?? undefined,
            mode: url.searchParams.get('mode') ?? undefined,
        })
        lsp = await createLSP(query.language, query.mode)
        console.log('created lsp', lsp.container.id)
        socket.on('close', () => {
            clearLSP(lsp.container).catch(e => {
                console.error('Не удалось удалить LSP', e)
            })
        })
    } catch (error) {
        const closeError = toCloseError(error);
        console.error('failed to create lsp', error);
        wss.handleUpgrade(request, socket, head, (webSocket) => {
            webSocket.close(closeError.closeCode, closeReason(closeError.message));
        });
        return;
    }

    wss.handleUpgrade(request, socket, head, (webSocket) => {
        lsp.container.wait().then(() => {
            webSocket.close(1000, 'container killed')
        })
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
            onClose: (cb) => {
                webSocket.on("close", cb);
            },
            dispose: () => webSocket.close(),
        };
        // launch the server when the web socket is opened
        if (webSocket.readyState === webSocket.OPEN) {
            launchLanguageServer(socket, lsp);
        } else {
            webSocket.on("open", () => {
                launchLanguageServer(socket, lsp);
            });
        }
    });
})
