import {Editor} from "@monaco-editor/react";
import React, {useRef, useState} from "react";
import {Badge, Button, Flex, Group, SegmentedControl, Text} from "@mantine/core";
import {editor} from "monaco-editor";
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {codeExamples} from "./CodeExamples.tsx";
import {IconArrowAutofitLeft, IconBrain, IconCoin, IconGripVertical, IconMoodCrazyHappy} from "@tabler/icons-react";
import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {Link} from "react-router-dom";
import {SSE} from "sse.js";
import ICodeEditor = editor.ICodeEditor;

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
  const [language, setLanguage] = useState<LanguageValue>("Javascript")
  const editorRef = useRef<ICodeEditor>()

  const [messages, setMessages] = useState<RunnerEvent[]>([])
  const [running, setRunning] = useState(false)

  const handleRun = async () => {
    const code = editorRef.current?.getValue() ?? ''
    setMessages([])
    setRunning(true)
    const source = new SSE('/api/playground', {
      headers: {
        'Content-Type': 'application/json',
      },
      payload: JSON.stringify({code, language} as RunRequest),
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
    source.addEventListener('readystatechange', (e: {readyState: number}) => {
      if(e.readyState !== source.OPEN) {
        setRunning(false)
      }
    })
  }

  const keyboardHandler = (e: React.KeyboardEvent) => {
    if (e.key === 'F9') {
      handleRun()
    }
  }

  const onLanguageChange = (value: string) => {
    setLanguage(value as LanguageValue)
    editorRef.current?.setValue(codeExamples[value as LanguageValue])
  };
  const onEditorMount = (editor: ICodeEditor) => {
    console.warn('editor initialized', editor)
    editorRef.current = editor
  }

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
          data={[
            {
              value: 'Cpp',
              label: <Flex align={'center'} gap={4}><IconBrain/><Text size={'lg'}>C++17 gcc:13.2.0</Text></Flex>
            },
            {
              value: 'Java',
              label: <Flex align={'center'} gap={4}><IconCoin/><Text size={'lg'}>Java 21 corretto</Text></Flex>,
            },
            {
              value: 'Javascript',
              label: <Flex align={'center'} gap={4}><IconMoodCrazyHappy/><Text size={'lg'}>node.js
                20</Text></Flex>,
            }
          ]} value={language} onChange={onLanguageChange}
        />
      </Flex>
      <PanelGroup className={'mt-1'} autoSaveId={'playground-panel-group'} direction={'horizontal'}>
        <Panel defaultSize={70} className={'pt-1 rounded-xl bg-white'}>
          <Editor onMount={onEditorMount} height="100%" language={language.toLowerCase()}
                  defaultValue={codeExamples[language]}
                  loading={
                    <Text>
                      Редактор кода загружается
                    </Text>
                  }
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

