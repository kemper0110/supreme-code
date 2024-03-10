import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {useEffect, useRef, useState} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {IconGripHorizontal, IconGripVertical} from "@tabler/icons-react";
 import {Button, Flex, Loader, Pill, PillGroup, SegmentedControl, Stack, Title} from "@mantine/core";
import {useParams} from "react-router-dom";
import {useMutation, useQuery} from "@tanstack/react-query";
import axios from "axios";
import {CppView, JavaView, NodeView} from "../components/LanguageView.tsx";
import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";
import ICodeEditor = editor.ICodeEditor;
import {LanguageValue} from "../types/LanguageValue.tsx";

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
  result: TestResultData
}

type TestResultData = {
  solutionId: number
  tests: number
  failures: number
  errors: number
  time: number
  xml: string
  logs: string
  statusCode: number
}

export default function Problem() {
  const {slug} = useParams()
  const {data} = useQuery<ProblemData>({queryKey: ['problem', slug]})
  const {name, description, languages, result: lastResult} = data!

  const languageSelectorData =
    languages.map(l => ({
      label: {Cpp: <CppView/>, Java: <JavaView/>, Javascript: <NodeView/>}[l.language],
      value: l.language
    }))

  const [selectedLanguage, setSelectedLanguage] = useState(languages[0])


  const editorRef = useRef<ICodeEditor>()
  useEffect(() => editorRef.current?.setValue(selectedLanguage.template), [selectedLanguage.language])

  const testMutation = useMutation({
    mutationFn: (code: string) => axios.post<TestResultData>(`/api/problem/${encodeURIComponent(slug!)}`, {
      language: selectedLanguage.language,
      code
    }),
    onSuccess: response => {
      console.log(response.data)
    }
  })


  const onRunClick = () => {
    // @ts-ignore
    testMutation.mutate(editorRef.current?.getValue())
  }

  const result = testMutation.data ? testMutation.data.data : lastResult

  return (
    <div className={'h-screen'}>
      <PanelGroup autoSaveId={'problem:[description-editor]'} direction={'horizontal'}>
        <Panel defaultSize={40}>
          <Title pl={16}>
            {name}
          </Title>
          <Markdown remarkPlugins={[remarkGfm]} className={'p-4 pb-16 prose max-w-full overflow-y-auto max-h-screen'}>
            {description}
          </Markdown>
        </Panel>
        <PanelResizeHandle className={'bg-slate-200 flex items-center justify-center'}>
          <IconGripVertical className={'w-[15px] text-slate-500'}/>
        </PanelResizeHandle>
        <Panel>
          <PanelGroup autoSaveId={'problem:[code-test]'} direction={'vertical'}>
            <Panel className={'pb-[60px]'} defaultSize={80}>
              <Editor onMount={editor => editorRef.current = editor}
                      height="100%" language={selectedLanguage.language.toLowerCase()}
                      defaultValue={selectedLanguage.template}/>
              <Flex justify={'end'} gap={12} align={'center'} pr={20}>
                <SegmentedControl size={'xs'} data={languageSelectorData} value={selectedLanguage.language} onChange={value => {
                  setSelectedLanguage(languages.find(l => l.language === value)!)
                }}/>
                <Button onClick={onRunClick}>
                  Запустить
                </Button>
              </Flex>
            </Panel>
            <PanelResizeHandle className={' bg-slate-200 flex items-center justify-center'}>
              <IconGripHorizontal className={'h-[15px] text-slate-500'}/>
            </PanelResizeHandle>
            <Panel className={'!overflow-auto'}>
              {/*<Test step={step}/>*/}
              {
                testMutation.isPending ? (
                  <div className={'w-full h-full flex items-center justify-center'}>
                    <Loader/>
                  </div>
                ) : null
              }
              {result ? <ResultPanel result={result}/> : null}
            </Panel>
          </PanelGroup>
        </Panel>
      </PanelGroup>
    </div>
  )
}

const ResultPanel = ({result} : {result: TestResultData}) => {
  return (
    <Stack className={'h-full'} p={8}>
      <PillGroup>
        <Pill>
          status {result.statusCode}
        </Pill>
        <Pill>
          {result.tests} tests
        </Pill>
        <Pill>
          {result.errors} errors
        </Pill>
        <Pill>
          {result.failures} failures
        </Pill>
        <Pill>
          time {result.time}s
        </Pill>
      </PillGroup>
      <Flex className={'shrink grow gap-2'}>
                      <pre className={'w-1/2 overflow-auto rounded-lg border-2 border-slate-200 shadow-md shadow-slate-200 p-2'}>
                        {result.logs}
                      </pre>
        <pre className={'w-1/2 overflow-auto rounded-lg border-2 border-slate-200 shadow-md shadow-slate-200 p-2'}>
                        {result.xml}
                      </pre>
      </Flex>
    </Stack>
  )
}

// const Test = ({step}: { step: number }) => {
//
//   return (
//     <div className={'p-4'}>
//       <Stepper active={step}>
//         <Stepper.Step label="Шаг 1" description="Отправить код на тесты">
//           Шаг 1: Отправить код на тесты
//         </Stepper.Step>
//         <Stepper.Step label="Шаг 2" description="Сборка">
//           Шаг 2: Сборка
//         </Stepper.Step>
//         <Stepper.Step label="Шаг 3" description="Тестирование">
//           Шаг 3: Тестирование
//         </Stepper.Step>
//         <Stepper.Completed>
//           Все тесты прошли успешно
//         </Stepper.Completed>
//       </Stepper>
//     </div>
//   )
// }
