import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {useEffect, useRef, useState} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {Link, RichTextEditor} from '@mantine/tiptap';
import {useEditor} from '@tiptap/react';
import Highlight from '@tiptap/extension-highlight';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Superscript from '@tiptap/extension-superscript';
import SubScript from '@tiptap/extension-subscript';
import {IconGripHorizontal, IconGripVertical} from "@tabler/icons-react";
import {Button, Flex, Loader, Pill, PillGroup, SegmentedControl, Stack, Stepper, Title} from "@mantine/core";
import {useParams} from "react-router-dom";
import {useMutation, useQuery} from "@tanstack/react-query";
import axios from "axios";
import {LanguageValue} from "../types/LanguageValue.tsx";
import {CppView, JavaView, NodeView} from "../components/LanguageView.tsx";
import ICodeEditor = editor.ICodeEditor;

type Language = {
  id: number
  language: LanguageValue
  template: string
}

type ProblemData = {
  id: number
  name: string
  active: boolean
  description: string
  difficulty: 'Easy' | 'Normal' | 'Hard'
  languages: Language[]
}

type TestResultData = {
  tests: number
  failures: number
  errors: number
  time: number
  xml: string
  logs: string
}

export default function Problem() {
  const {id} = useParams()
  const {data} = useQuery<ProblemData>({queryKey: ['problem', id]})
  const {name, description, languages} = data!

  const languageSelectorData =
    languages.map(l => ({
      label: {Cpp: <CppView/>, Java: <JavaView/>, Javascript: <NodeView/>}[l.language],
      value: l.language
    }))

  const [selectedLanguage, setSelectedLanguage] = useState(languages[0])

  const step = 0

  const editorRef = useRef<ICodeEditor>()
  useEffect(() => editorRef.current?.setValue(selectedLanguage.template), [selectedLanguage.language])

  const testMutation = useMutation({
    mutationFn: (code: string) => axios.post<TestResultData>(`/api/problem/${encodeURIComponent(id!)}`, {
      language: selectedLanguage.language,
      code
    }),
    onSuccess: response => {
      console.log(response.data)
    }
  })

  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      Link,
      Superscript,
      SubScript,
      Highlight,
      TextAlign.configure({types: ['heading', 'paragraph']}),
    ],
    content: description,
    editable: false,
  });

  const onRunClick = () => {
    // @ts-ignore
    testMutation.mutate(editorRef.current?.getValue())
  }

  return (
    <div className={'h-screen'}>
      <PanelGroup autoSaveId={'problem:[description-editor]'} direction={'horizontal'}>
        <Panel defaultSize={40}>
          <Title>
            {name}
          </Title>
          <RichTextEditor editor={editor}>
            <RichTextEditor.Content className={'overflow-y-scroll max-h-screen'}/>
          </RichTextEditor>
        </Panel>
        <PanelResizeHandle className={'bg-slate-200 flex items-center justify-center'}>
          <IconGripVertical className={'w-[15px] text-slate-500'}/>
        </PanelResizeHandle>
        <Panel>
          <PanelGroup autoSaveId={'problem:[code-test]'} direction={'vertical'}>
            <Panel className={'pb-[60px]'} defaultSize={80}>
                <Editor onMount={editor => editorRef.current = editor}
                        height="100%" language={selectedLanguage.language.toLowerCase()} defaultValue={selectedLanguage.template}/>
                <Flex justify={'end'} gap={12} align={'center'} pr={20}>
                  <SegmentedControl data={languageSelectorData} value={selectedLanguage.language} onChange={value => {
                    setSelectedLanguage(languages.find(l => l.language === value)!)
                  }}/>
                  <Button onClick={onRunClick}>
                    Запустить
                  </Button>
                </Flex>
            </Panel>
            <PanelResizeHandle className={'my-[10px] bg-slate-200 flex items-center justify-center'}>
              <IconGripHorizontal className={'h-[15px] text-slate-500'}/>
            </PanelResizeHandle>
            <Panel>
              {/*<Test step={step}/>*/}
              {
                testMutation.isPending ? (
                  <div className={'w-full h-full flex items-center justify-center'}>
                    <Loader/>
                  </div>
                ) : null
              }
              {
                testMutation.data ? (
                  <Stack mah={'100%'}>
                    <PillGroup>
                      <Pill>
                        {testMutation.data.data.tests} tests
                      </Pill>
                      <Pill>
                        {testMutation.data.data.errors} errors
                      </Pill>
                      <Pill>
                        {testMutation.data.data.failures} failures
                      </Pill>
                      <Pill>
                        {testMutation.data.data.time}s time
                      </Pill>
                    </PillGroup>
                    <Flex className={'shrink grow'} mah={'240px'}>
                        <pre className={'w-1/2 overflow-auto border'}>
                          {testMutation.data.data.logs}
                        </pre>
                      <pre className={'w-1/2 overflow-auto border'}>
                          {testMutation.data.data.xml}
                        </pre>
                    </Flex>
                  </Stack>
                ) : null
              }
            </Panel>
          </PanelGroup>
        </Panel>
      </PanelGroup>
    </div>
  )
}

const Test = ({step}: { step: number }) => {

  return (
    <div className={'p-4'}>
      <Stepper active={step}>
        <Stepper.Step label="Шаг 1" description="Отправить код на тесты">
          Шаг 1: Отправить код на тесты
        </Stepper.Step>
        <Stepper.Step label="Шаг 2" description="Сборка">
          Шаг 2: Сборка
        </Stepper.Step>
        <Stepper.Step label="Шаг 3" description="Тестирование">
          Шаг 3: Тестирование
        </Stepper.Step>
        <Stepper.Completed>
          Все тесты прошли успешно
        </Stepper.Completed>
      </Stepper>
    </div>
  )
}
