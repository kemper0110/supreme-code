import React, {useRef, useState} from "react";
import MonacoEditor from "react-monaco-editor";
import {Badge, Button, Flex, Group, SegmentedControl, Text} from "@mantine/core";
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {IconArrowAutofitLeft, IconGripVertical} from "@tabler/icons-react";
import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {Link} from "react-router-dom";
import {SSE} from "sse.js";
import * as monaco from 'monaco-editor';
import {usePlatformConfigQuery} from "../shared/PlatformConfig.ts";
import {LanguageTitle} from "../../components/LanguageTitle.tsx";
import {useMonacoLsp} from "../../hooks/useMonacoLsp.ts";
import {sortedLanguageEntries} from "../shared/languageSorting.ts";
import {keycloak} from "../../keycloak.ts";

const RUN_WORK_DIR = '/usr/run';

const playgroundModelPath = (language: { runnerConfig?: { filePath: string }, ephemeralFileName: string }) =>
  `${RUN_WORK_DIR}/${language.runnerConfig?.filePath ?? language.ephemeralFileName}`;

type RunRequest = {
  code: string
  language: string
}

type LogEvent = {
  type: 'log'
  message: string
}
type ErrorEvent = {
  type: 'error'
  message: string
}
type InfoEvent = {
  type: 'info'
  message: string
}
type RunnerEvent = LogEvent | ErrorEvent | InfoEvent

export default function Playground() {
  const {data: platformConfig} = usePlatformConfigQuery()

  const [languageId, setLanguageId] = useState<string>(sortedLanguageEntries(platformConfig!.languages)[0][0])
  const language = platformConfig!.languages[languageId]!

  const [messages, setMessages] = useState<RunnerEvent[]>([])
  const [running, setRunning] = useState(false)
  const [value, setValue] = useState(language.playgroundInitialCode)
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null)
  useMonacoLsp(language, platformConfig?.languages, 'run')

  const handleRun = async () => {
    setMessages([])
    setRunning(true)
    const source = new SSE('/api/playground', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${keycloak.token}`
      },
      payload: JSON.stringify({code: value, language: languageId} as RunRequest),
      method: 'POST',
    })
    const handler = (event: MessageEvent, type: RunnerEvent['type']) => {
      const message = event.data
      console.log(message)
      setMessages(messages => [...messages, {type, message}])
    }
    source.addEventListener('log', (e: MessageEvent) => handler(e, 'log'))
    source.addEventListener('error', (e: MessageEvent) => handler(e, 'error'))
    source.addEventListener('info', (e: MessageEvent) => handler(e, 'info'))
    source.addEventListener('readystatechange', (e: { readyState: number }) => {
      if (e.readyState !== source.OPEN) {
        setRunning(false)
      }
    })
  }

  const keyboardHandler = (e: React.KeyboardEvent) => {
    if (e.key === 'F9') {
      handleRun()
    }
  }

  console.log(monaco.editor.getModels())

  const onLanguageChange = (value: string) => {
    const newLanguage = platformConfig!.languages[value]
    editorRef.current!.getModel()?.dispose()
    editorRef.current!.setModel(monaco.editor.createModel(
      newLanguage.playgroundInitialCode,
      newLanguage.monacoLanguageId,
      monaco.Uri.file(playgroundModelPath(newLanguage))
    ))

    setLanguageId(value as LanguageValue)
    setValue(newLanguage.playgroundInitialCode)
  };

  return (
    <div onKeyDown={keyboardHandler}
         className={'flex flex-col px-3 pt-2 pb-4 h-screen bg-gray-100 [body:bg-gray-100] gap-1'}>
      <Flex justify={'space-between'} align={'center'}>
        <Link to={'/'}>
          <IconArrowAutofitLeft size={32}/>
        </Link>
        <Group align={'center'}>
          <Button onClick={handleRun}>
            Выполнить
          </Button>
          <Text>
            {
              running ? (
                <Badge color={'blue'}>Выполняется</Badge>
              ) : (
                <Badge color={'teal'}>Завершено</Badge>
              )
            }
          </Text>
        </Group>

        <SegmentedControl
          data={sortedLanguageEntries(platformConfig!.languages).map(([key, lang]) => ({
            value: key,
            label: <LanguageTitle language={lang}/>
          }))}
          value={languageId} onChange={onLanguageChange}
        />
      </Flex>
      <PanelGroup className={'mt-1'} autoSaveId={'playground-panel-group'} direction={'horizontal'}>
        <Panel defaultSize={70} className={'pt-1 rounded-xl bg-white'}>
          <MonacoEditor
            value={value}
            onChange={(value) => setValue(value)}
            editorDidMount={(editor) => {
              editorRef.current = editor
              editor.setModel(monaco.editor.createModel(value, language.monacoLanguageId, monaco.Uri.file(playgroundModelPath(language))))
            }}
            language={language.monacoLanguageId}
            width={"calc(100vw - 50px)"}
            options={{automaticLayout: true}}
          />
        </Panel>
        <PanelResizeHandle className={'flex items-center justify-center'}>
          <IconGripVertical className={'w-[15px] text-slate-500'}/>
        </PanelResizeHandle>
        <Panel className={'rounded-xl bg-white'}>
          <pre className={'px-4 pt-4 overflow-auto h-full'}>
            {
              messages.map((message, id) => (
                <span className={{
                  log: 'text-slate-800',
                  error: 'text-red-500',
                  info: 'text-blue-600'
                }[message.type]} key={id}>
                  {message.message}
                </span>
              ))
            }
          </pre>
        </Panel>
      </PanelGroup>
    </div>
  )
}

