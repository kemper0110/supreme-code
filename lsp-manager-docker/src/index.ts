import {WebSocketServer} from "ws";

import {env} from "./env.js";
import {z} from "zod";
import {Message} from "vscode-languageserver-protocol";
import Docker, {type Container} from 'dockerode';
import * as fs from "node:fs";
import * as stream from "node:stream";
import {PassThrough} from "node:stream";
import {type IWebSocket, WebSocketMessageReader, WebSocketMessageWriter} from "vscode-ws-jsonrpc";
import {createConnection, createStreamConnection} from "vscode-ws-jsonrpc/server";
import * as http from "node:http";
import {IncomingMessage} from "node:http";
import {Socket} from "node:net";
import {URL} from "node:url";

const docker = new Docker();

const ConfigSchema = z.object({
    languages: z.record(z.string(), z.object({
        image: z.string(),
    }))
})

const config = ConfigSchema.parse(JSON.parse(fs.readFileSync('./config.json', 'utf8')))

const querySchema = z.object({
    language: z.string(),
})

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

async function createLSP(language: string): Promise<LSP> {
    const languageConfig = config.languages[language]
    if (!languageConfig)
        throw new Error("language not found")

    const container = await docker.createContainer({
        Image: languageConfig.image,
        Tty: false,
        AttachStdout: true,
        AttachStdin: true,
        AttachStderr: true,
        OpenStdin: true,
        StdinOnce: false,
    });

    await container.start();
    const attach = await container.attach({
        stream: true,
        stdin: true,
        stdout: true,
        stderr: true,
    });
    const stdout = new PassThrough();
    const stderr = new PassThrough();
    container.modem.demuxStream(attach, stdout, stderr);

    stderr.on('data', d => {
        console.error('[LSP stderr]', d.toString());
    });

    await waitForContainerReady(container)

    return {
        container,
        stdout,
        // convert `attach` as WritableStream to Writable :)
        stdin: stream.Writable.fromWeb(stream.Writable.toWeb(attach)),
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
    socketConnection.onClose(() => serverConnection.dispose());
    serverConnection.onClose(() => socketConnection.dispose());
}

server.on('upgrade', async (request: IncomingMessage, socket: Socket, head: Buffer) => {
    const url = new URL(request.url!, `http://${request.headers.host}/`);
    const lsp = await createLSP(url.searchParams.get('language')!);
    console.log('created lsp', lsp.container.id)

    wss.handleUpgrade(request, socket, head, (webSocket) => {
        webSocket.on('close', () => {
            clearLSP(lsp.container).catch(e => {
                console.error('Не удалось удалить LSP', e)
            })
        })
        lsp.container.wait().then(() => {
            webSocket.close(1101, 'container killed')
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
