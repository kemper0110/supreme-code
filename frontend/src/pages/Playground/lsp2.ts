import {
  RegisteredFileSystemProvider,
  RegisteredMemoryFile,
  registerFileSystemOverlay
} from '@codingame/monaco-vscode-files-service-override';
import getKeybindingsServiceOverride from '@codingame/monaco-vscode-keybindings-service-override';
import * as vscode from 'vscode';
import {LogLevel} from '@codingame/monaco-vscode-api';
import {EditorApp, type EditorAppConfig} from 'monaco-languageclient/editorApp';
import {type LanguageClientConfig, LanguageClientWrapper} from 'monaco-languageclient/lcwrapper';
import {type MonacoVscodeApiConfig, MonacoVscodeApiWrapper} from 'monaco-languageclient/vscodeApiWrapper';
import {configureDefaultWorkerFactory} from 'monaco-languageclient/workerFactory';
import {type ConnectionConfigOptions} from 'monaco-languageclient/common';


export const runExtendedClient = async (lsConfig: ExampleLsConfig, helloCode: string) => {
  const prefix = '/workspace'
  const helloUri = vscode.Uri.file(prefix + `/main.ts`);
  const denoJsonUri = vscode.Uri.file(prefix + `/deno.json`);
  const fileSystemProvider = new RegisteredFileSystemProvider(false);
  fileSystemProvider.registerFile(new RegisteredMemoryFile(helloUri, helloCode));
  fileSystemProvider.registerFile(new RegisteredMemoryFile(denoJsonUri, '{}'));
  registerFileSystemOverlay(1, fileSystemProvider);

  const htmlContainer = document.getElementById('monaco-editor-root')!;
  const vscodeApiConfig: MonacoVscodeApiConfig = {
    $type: 'extended',
    viewsConfig: {
      $type: 'EditorService',
      htmlContainer
    },
    logLevel: LogLevel.Debug,
    serviceOverrides: {
      ...getKeybindingsServiceOverride()
    },
    userConfiguration: {
      json: JSON.stringify({
        'workbench.colorTheme': 'Default Dark Modern',
        'editor.guides.bracketPairsHorizontal': 'active',
        'editor.lightbulb.enabled': 'On',
        'editor.wordBasedSuggestions': 'off',
        'editor.experimental.asyncTokenization': true,
        // Включите suggestions
        'editor.quickSuggestions': {
          'other': true,
          'comments': false,
          'strings': true
        },
        'editor.suggestOnTriggerCharacters': true,
        'editor.acceptSuggestionOnEnter': 'on'
      })
    },
    monacoWorkerFactory: configureDefaultWorkerFactory
  };

  const startOptions = {
    onCall: () => {
      console.log('Connected to socket.');
    },
    reportStatus: true
  };
  const stopOptions = {
    onCall: () => {
      console.log('Disconnected from socket.');
    },
    reportStatus: true
  };

  let connectionConfigOptions: ConnectionConfigOptions;
  const webSocketUrl = `ws://localhost:${lsConfig.port}${lsConfig.path}`;
  connectionConfigOptions = {
    $type: 'WebSocketUrl',
    url: webSocketUrl,
    startOptions,
    stopOptions
  };

  const languageClientConfig: LanguageClientConfig = {
    languageId: 'typescript',
    connection: {
      options: connectionConfigOptions
    },
    clientOptions: {
      documentSelector: ['typescript'],
      workspaceFolder: {
        index: 0,
        name: 'workspace',
        uri: vscode.Uri.parse(prefix)
      },
      middleware: {
        didOpen: (document, next) => {
          console.log('📄 Document opened:', document.uri.toString());
          console.log('📄 Document content length:', document.getText().length);
          return next(document);
        },
        didChange: (event, next) => {
          console.log('📝 Document changed:', event.document.uri.toString());
          return next(event);
        },
        provideCompletionItem: async (document, position, context, token, next) => {
          console.log('🔍 Completion requested at line:', position.line, 'character:', position.character);
          const result = await next(document, position, context, token);
          console.log('✅ Completion result:', result?.length || 0, 'items');
          return result;
        }
      }
    }
  };

  const editorAppConfig: EditorAppConfig = {
    codeResources: {
      modified: {
        text: helloCode,
        uri: helloUri.path
      }
    },
  };

  // perform global init
  const apiWrapper = new MonacoVscodeApiWrapper(vscodeApiConfig);
  await apiWrapper.start();

  // Создаем обертки
  const lcWrapper = new LanguageClientWrapper(languageClientConfig);
  const editorApp = new EditorApp(editorAppConfig);

  // Запускаем редактор и LSP последовательно
  await editorApp.start(htmlContainer);
  await lcWrapper.start();

  await new Promise(resolve => setTimeout(resolve, 1000));

  const doc = await vscode.workspace.openTextDocument(helloUri);
  await vscode.window.showTextDocument(doc);

  vscode.workspace.onDidChangeTextDocument((event) => {
    if (event.document.uri.toString() === helloUri.toString()) {
      console.log('📝 Document changed, new length:', event.document.getText().length);
    }
  });
};

export type ExampleLsConfig = {
  port: number;
  path: string;
  languageId: string;
  useExternalWebSocket: boolean;
};
