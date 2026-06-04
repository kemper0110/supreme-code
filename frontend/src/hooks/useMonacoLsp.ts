import {useEffect} from "react";
import * as monaco from "monaco-editor";
import {CloseAction, ErrorAction, MonacoLanguageClient, MonacoServices} from "monaco-languageclient";
import {toSocket, WebSocketMessageReader, WebSocketMessageWriter} from "vscode-ws-jsonrpc";
import {createMessageConnection} from "vscode-jsonrpc";
import type {PlatformLanguage} from "../pages/Problem/Loader.tsx";
import {lspWebSocketUrl} from "../config.ts";

let monacoServicesInstalled = false;
const registeredLanguages = new Set<string>();
export type LspMode = 'run' | 'test';

const lspWorkspaceByMode: Record<LspMode, string> = {
  run: '/usr/run',
  test: '/usr/test',
};

export function useMonacoLsp(
  language: PlatformLanguage | undefined,
  languages: Record<string, PlatformLanguage> | undefined,
  mode: LspMode = 'run'
) {
  useEffect(() => {
    if (!languages) {
      return;
    }

    if (!monacoServicesInstalled) {
      MonacoServices.install();
      monacoServicesInstalled = true;
    }

    Object.values(languages).forEach(language => {
      const languageId = language.monacoLanguageId;
      if (registeredLanguages.has(languageId)) {
        return;
      }

      monaco.languages.register({
        id: languageId,
        extensions: language.extensions,
        aliases: [languageId],
      });
      registeredLanguages.add(languageId);
    });
  }, [languages]);

  useEffect(() => {
    if (!language) {
      return;
    }

    try {
      const url = new URL(lspWebSocketUrl);
      url.searchParams.set('language', language.monacoLanguageId);
      url.searchParams.set('mode', mode);
      const webSocket = new WebSocket(url.toString());

      let languageClient: MonacoLanguageClient | undefined;
      webSocket.onopen = () => {
        const socket = toSocket(webSocket);
        const messageReader = new WebSocketMessageReader(socket);
        const messageWriter = new WebSocketMessageWriter(socket);
        const connection = createMessageConnection(messageReader, messageWriter, console);
        connection.onClose(() => connection.dispose());
        console.log('Connection established');
        languageClient = new MonacoLanguageClient({
          name: language.monacoLanguageId + ' Language Server',
          clientOptions: {
            workspaceFolder: {
              uri: monaco.Uri.file(lspWorkspaceByMode[mode]),
              index: 0,
              name: mode,
            },
            documentSelector: [language.monacoLanguageId],
            errorHandler: {
              error: (error, message) => {
                console.error(message, error);
                return {
                  action: ErrorAction.Continue,
                };
              },
              closed: () => {
                return {
                  action: CloseAction.Restart,
                };
              },
            },
          },
          connectionProvider: {
            get: () => Promise.resolve({
              reader: messageReader,
              writer: messageWriter,
            }),
          },
        });
        languageClient.start();
      };

      webSocket.onerror = evt => {
        console.error(evt);
      };
      return () => {
        webSocket.close();
        languageClient?.stop();
      };
    } catch (e) {
      console.error(e);
      return;
    }
  }, [language, mode]);
}
