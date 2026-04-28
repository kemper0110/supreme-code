import {Hono} from "hono";
import {serve} from "@hono/node-server";
import {createNodeWebSocket} from '@hono/node-ws'
import {env} from "./env.js";
import * as k8s from '@kubernetes/client-node'
import {V1Pod, V1Service} from '@kubernetes/client-node'
import {z} from "zod";
import * as fs from "node:fs";
import jsyaml from "js-yaml";
import {customAlphabet} from "nanoid";
import {cors} from 'hono/cors'
import type {WSMessageReceive} from "hono/dist/types/helper/websocket/index.js";

const kc = new k8s.KubeConfig();
kc.loadFromDefault();
const client = kc.makeApiClient(k8s.CoreV1Api);

const app = new Hono();
app.use('*', cors({
    origin: '*',
    allowHeaders: ['*'],
    allowMethods: ['*'],
    exposeHeaders: ['*'],
    maxAge: 600,
    credentials: true,
}))
const {injectWebSocket, upgradeWebSocket} = createNodeWebSocket({app})

const querySchema = z.object({
    language: z.string(),
})

type LSP = {
    url: string
    name: string
}

async function createLSP(language: string, userId: string, id: string) {
    const labels = {
        'lsp-language': language,
        'lsp-user': userId,
    }
    const name = `lsp-${userId}-${language}-${id}`

    const podTemplate = jsyaml.load(await fs.promises.readFile('./templates/podManifest.yaml', 'utf-8')) as V1Pod;
    await client.createNamespacedPod({
        namespace: env.NAMESPACE,
        body: {
            ...podTemplate,
            metadata: {
                ...podTemplate.metadata,
                labels: {
                    ...podTemplate.metadata?.labels,
                    ...labels,
                },
                name: name,
            },
            spec: {
                ...podTemplate.spec,
                containers: [
                    ...(podTemplate.spec?.containers ?? []),
                    {
                        name: 'lsp',
                        image: 'denols',
                        ports: [
                            {
                                containerPort: 30_002,
                            }
                        ],
                        env: [
                            {
                                name: 'serverName',
                                value: language,
                            },
                            {
                                name: 'pathName',
                                value: '/',
                            },
                            {
                                name: 'serverPort',
                                value: '30002',
                            },
                            {
                                name: 'runCommand',
                                value: 'deno',
                            },
                            {
                                name: 'runCommandArgs',
                                value: 'lsp',
                            },
                            {
                                name: 'runCommandCwd',
                                value: '/workspace',
                            },
                        ]
                    }
                ]
            }
        }
    })

    const serviceTemplate = jsyaml.load(await fs.promises.readFile('./templates/serviceManifest.yaml', 'utf-8')) as V1Service;

    await client.createNamespacedService({
        namespace: env.NAMESPACE,
        body: {
            ...serviceTemplate,
            metadata: {
                ...serviceTemplate.metadata,
                labels: {
                    ...serviceTemplate.metadata?.labels,
                    ...labels,
                },
                name: name,
            },
            spec: {
                ...serviceTemplate.spec,
                type: 'ClusterIP',
                selector: {
                    ...serviceTemplate.spec?.selector,
                    ...labels,
                },
                ports: [
                    ...(serviceTemplate.spec?.ports ?? []),
                    {
                        port: 30002,
                        targetPort: 30002,
                    }
                ]
            }
        }
    })

    const watch = new k8s.Watch(kc)

    const {promise, resolve, reject} = Promise.withResolvers<void>()
    let path = '/api/v1/namespaces/default/pods'
    await watch.watch(path, {}, (type, apiObj, watchObj) => {
        // console.log(type, apiObj, watchObj);
        if (apiObj.kind === 'Pod') {
            const pod = apiObj as V1Pod
            if (
                pod.metadata?.labels?.["lsp-language"] == labels["lsp-language"]
                && pod.metadata?.labels?.["lsp-user"] == labels["lsp-user"]
            ) {
                const condition = pod.status?.conditions?.find(c => c.type === 'Ready')
                const ready = condition?.status === 'True'
                if (ready) {
                    resolve()
                }
            }
        }
    }, (err) => {
        if (err) {
            reject(err)
        } else {
            resolve()
        }
    })
    await promise;

    return {
        url: `ws://${name}:30002/`,
        name,
    }
}

async function clearLSP(name: string) {
    await client.deleteNamespacedService({
        namespace: env.NAMESPACE,
        name,
    })
    await client.deleteNamespacedPod({
        namespace: env.NAMESPACE,
        name,
    })
}


let incrementId = 0;

app.get("/ws", upgradeWebSocket(async (c) => {
    const query = querySchema.parse(c.req.query());
    // const auth = c.req.header('authorization')
    // if (!auth) {
    //     return c.text("Unauthorized", 401)
    // }
    // const jwtPayload = decode(auth.split(' ')[1]!, {json: true})
    // const userId = jwtPayload?.sub
    const userId = '2234'
    if (!userId) {
        return c.text("Unauthorized", 401)
    }
    const id = customAlphabet('1234567890')(8)

    const createStart = performance.now()
    const lsp = await createLSP(query.language, userId, id).catch(e => e)
    const createTime = performance.now() - createStart
    console.log('createTime', createTime)
    if (lsp instanceof Error) {
        throw lsp
    }
    const lspWs = new WebSocket(lsp.url)

    return {
        onOpen(_evt, client) {
            lspWs.onmessage = (event) => {
                client.send(event.data)
            }
            lspWs.onclose = () => {
                client.close()
            }
            lspWs.onerror = (evt) => {
                console.log('error', evt)
                client.close(1011, 'Upstream error')
            }
        },
        onMessage(event) {
            send(event.data)

            function send(data: WSMessageReceive) {
                if (lspWs.readyState === WebSocket.OPEN) {
                    lspWs.send(data)
                } else {
                    if (lspWs.readyState === WebSocket.CONNECTING)
                        setTimeout(() => send(data), 5)
                }
            }
        },
        onClose() {
            lspWs.close()
            clearLSP(lsp.name).catch(e => {
                console.error('Не удалось удалить LSP', e)
            })
        },
        onError(evt) {
            console.log('error', evt)
            lspWs.close()
        },
    }
}));

app.get("/", async (c) => {
    return c.text("Hello, world!")
})

const server = serve({
    fetch: app.fetch,
    port: env.PORT,
}, (info) => {
    console.log(`Listening on port ${info.port}`);
});
injectWebSocket(server);