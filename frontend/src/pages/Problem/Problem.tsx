import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import React, {createContext, ReactNode, useContext, useEffect, useReducer, useRef, useState} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {
  IconArrowAutofitLeft,
  IconCheck,
  IconFileDescription,
  IconGripHorizontal,
  IconGripVertical,
  IconReport
} from "@tabler/icons-react";
import {
  Button,
  Flex,
  Loader,
  Pill,
  PillGroup,
  ScrollArea,
  SegmentedControl,
  Stack,
  Table,
  Tabs,
  Title
} from "@mantine/core";
import {Link, useLocation, useParams} from "react-router-dom";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {CppView, JavaView, NodeView} from "../../components/LanguageView.tsx";
import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {LanguageValue} from "../../types/LanguageValue.tsx";
import cx from "clsx";
import classes from "../Problems/Problems.module.css";
import {useTabs} from "../../store/useTabs.tsx";
import {api} from "../../api/api.ts";
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
  solutions: Solution[]
}

type Solution = {
  id: number
  code: string
  problemSlug: string
  language: string
  solutionResult?: SolutionResult
}
type SolutionResult = {
  id: number
  tests: number
  failures: number
  errors: number
  time: number
  junitXml: string
  logs: string
  statusCode: number
  solved: boolean
}

const SelectedSolutionContext = createContext<[Solution | null, React.Dispatch<React.SetStateAction<Solution | null>>]>([null, () => {
  throw new Error("SelectedSolutionContext.Provider not found")
}])

export default function Problem() {
  const {slug} = useParams()
  const problemQueryKey = ['problem', slug]
  const {data} = useQuery<ProblemData>({queryKey: problemQueryKey})
  const {name, description, languages, solutions} = data!

  const location = useLocation()
  const pushTab = useTabs(state => state.push)
  pushTab({
    href: location.pathname,
    label: name
  })

  const selectedSolutionState = useState<Solution | null>(
    solutions && solutions.length > 0 ? solutions[0] : null
  )
  const [selectedSolution, setSelectedSolution] = selectedSolutionState
  useEffect(() => {
    console.log('array ref updated')
  }, [selectedSolutionState]);

  const languageSelectorData =
    languages.map(l => ({
      label: {Cpp: <CppView/>, Java: <JavaView/>, Javascript: <NodeView/>}[l.language],
      value: l.language
    }))

  const [selectedLanguage, setSelectedLanguage] = useState(languages[0])


  const editorRef = useRef<ICodeEditor>()
  useEffect(() => editorRef.current?.setValue(selectedLanguage.template), [selectedLanguage.language])

  const [activeTab, setActiveTab] = useState<'description' | 'solutions' | null>('description')

  const queryClient = useQueryClient()
  const testMutation = useMutation({
    mutationFn: (code: string) => api.post<Solution>(`/api/problem/${encodeURIComponent(slug!)}`, {
      language: selectedLanguage.language,
      code
    }),
    onMutate: () => {
      setActiveTab('solutions')
    },
    onSuccess: response => {
      const data = queryClient.getQueryData<ProblemData>(problemQueryKey)!
      queryClient.setQueryData(problemQueryKey, () => {
        return {
          ...data,
          solutions: [
            response.data,
            ...data.solutions
          ]
        }
      })
      setSelectedSolution(response.data)
      console.log(response.data)
    }
  })


  const onRunClick = () => {
    // @ts-ignore
    testMutation.mutate(editorRef.current?.getValue())
  }

  return (
    <SelectedSolutionContext.Provider value={selectedSolutionState}>
      <div className={'h-screen'}>
        <PanelGroup autoSaveId={'problem:[description-editor]'} direction={'horizontal'}>
          <Panel defaultSize={40}>
            <Flex pt={12} pb={4} gap={16} align={'center'}>
              <Link to={'/'}>
                <IconArrowAutofitLeft className={'ml-4'} size={32}/>
              </Link>
              <Title pb={4}>
                {name}
              </Title>
              {
                solutions.some(solution => solution.solutionResult?.solved) ? (
                  <span className={'rounded-full bg-green-200 p-2'}>
                    <IconCheck className={'text-green-600'} size={36}/>
                  </span>
                ) : null
              }
            </Flex>
            {/* @ts-ignore */}
            <Tabs value={activeTab} onChange={setActiveTab}>
              <Tabs.List>
                <Tabs.Tab value="description" leftSection={<IconFileDescription/>}>
                  Описание
                </Tabs.Tab>
                <Tabs.Tab value="solutions" leftSection={<IconReport/>}>
                  Решения
                </Tabs.Tab>
              </Tabs.List>

              <Tabs.Panel value="description">
                <Markdown remarkPlugins={[remarkGfm]}
                          className={'p-4 pb-16 prose max-w-full overflow-y-auto max-h-screen'}>
                  {description}
                </Markdown>
              </Tabs.Panel>

              <Tabs.Panel value="solutions">
                <SolutionsTable solutions={solutions}/>
              </Tabs.Panel>
            </Tabs>
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
                  <SegmentedControl size={'xs'} data={languageSelectorData} value={selectedLanguage.language}
                                    onChange={value => {
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
                {
                  testMutation.isPending ? (
                    <div className={'w-full h-full flex items-center justify-center'}>
                      <TestResultLoader/>
                    </div>
                  ) : (
                    selectedSolution ? <SolutionPanel solution={selectedSolution}/> :
                      <div className={'w-full h-full flex items-center justify-center'}>
                        {"Решений пока не предложено"}
                      </div>
                  )
                }
              </Panel>
            </PanelGroup>
          </Panel>
        </PanelGroup>
      </div>
    </SelectedSolutionContext.Provider>
  )
}

const TestResultLoader = ({messages = ['Тестируем ваш код', 'Результат без перезагрузки страницы', 'Пожалуйста, подождите'], timeout = 1000}: {
  messages?: ReactNode[]
  timeout?: number
}) => {
  const [messageId, nextMessage] = useReducer((state: number) => (state + 1) % messages.length, 0)
  const message = messages[messageId]

  useEffect(() => {
    const id = setInterval(nextMessage, timeout)
    return () => clearInterval(id)
  }, []);
  return (
    <div className={'flex flex-col gap-2 items-center'}>
      <Loader/>
      <span className={'font-medium text-slate-800'}>{message}</span>
    </div>
  )
}

/*
  v0: Solution отсутствует
  v1: пользователь запустил тест, отображаем спиннер
  v2: пользователь не дождался завершения и перезагрузил страницу / соединение оборвалось => инвалидация кеша страницы, отображение последнего сабмита, возможно еще без результата
  v3: пользователь пикнул Solution в истории =>
      v3.1: тестирование решения завершено => просто выводим результаты и логи
      v3.2: еще не завершено => выводим сообщение
 */
const SolutionPanel = ({solution}: { solution: Solution }) => {
  if (!solution.solutionResult) {
    return (
      <div className={'w-full h-full flex items-center justify-center'}>
        <span>{`Результат для решения #${solution.id} еще не доступен`}</span>
      </div>
    )
  }

  return (
    <Stack className={'h-full'} p={8}>
      <ResultPills solutionResult={solution.solutionResult}/>
      <Flex className={'shrink grow gap-2'}>
                      <pre
                        className={'w-1/2 overflow-auto rounded-lg border-2 border-slate-200 shadow-md shadow-slate-200 p-2'}>
                        {solution.solutionResult.logs}
                      </pre>
        <pre className={'w-1/2 overflow-auto rounded-lg border-2 border-slate-200 shadow-md shadow-slate-200 p-2'}>
                        {solution.solutionResult?.junitXml}
                      </pre>
      </Flex>
    </Stack>
  )
}

const ResultPills = ({solutionResult}: { solutionResult: SolutionResult }) => {
  return (
    <PillGroup>
      <Pill>
        статус код {solutionResult.statusCode}
      </Pill>
      <Pill>
        {solutionResult.tests} тестов
      </Pill>
      <Pill>
        {solutionResult.errors} ошибок
      </Pill>
      <Pill>
        {solutionResult.failures} сбоев
      </Pill>
      <Pill>
        время {solutionResult.time}с
      </Pill>
      <Pill fw={'bold'} className={solutionResult.solved ? '!text-green-700' : '!text-red-700'}>
        {solutionResult.solved ? 'Решена' : 'Не решена'}
      </Pill>
    </PillGroup>
  )
}

const SolutionsTable = ({solutions}: { solutions: Solution[] }) => {
  const [scrolled, setScrolled] = useState(false);

  function Row({solution}: { solution: Solution }) {
    const setSelectedSolution = useContext(SelectedSolutionContext)[1]
    return (
      <Table.Tr key={solution.id} className={'hover:bg-slate-50 transition-colors cursor-pointer'}
                onClick={() => setSelectedSolution(solution)}
      >
        <Table.Td>{solution.id}</Table.Td>
        <Table.Td>{solution.language}</Table.Td>
        <Table.Td>
          {
            solution.solutionResult ? <ResultPills solutionResult={solution.solutionResult}/> : <Pill>Pending</Pill>
          }
        </Table.Td>
      </Table.Tr>
    )
  }

  return (
    <ScrollArea h={'100%'} onScrollPositionChange={({y}) => setScrolled(y !== 0)}>
      <Table miw={700}>
        <Table.Thead className={cx(classes.header, {[classes.scrolled]: scrolled})}>
          <Table.Tr>
            <Table.Th>#</Table.Th>
            <Table.Th>Язык</Table.Th>
            <Table.Th>Результат</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {solutions.map(solution => <Row key={solution.id} solution={solution}/>)}
        </Table.Tbody>
      </Table>
    </ScrollArea>
  )
}
