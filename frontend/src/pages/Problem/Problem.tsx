import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {MutableRefObject, useEffect, useRef, useState} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {
  IconArrowAutofitLeft,
  IconBrain,
  IconCheck,
  IconGripHorizontal,
  IconGripVertical,
  IconMoodCrazyHappy,
  IconPigMoney,
  IconRestore
} from "@tabler/icons-react";
import {ActionIcon, Button, CopyButton, Flex, HoverCard, SegmentedControl, Stack, Text, Title} from "@mantine/core";
import {Link, useParams} from "react-router-dom";
import {Language, Solution, useProblemQuery, useTestMutation} from "./Loader.tsx";
import {ResultPills} from "./components/components.tsx";
import {SelectedSolutionContext} from "./SelectedSolutionContext.tsx";
import {ProblemTabs} from "./Tabs/ProblemTabs.tsx";
import {TestResultLoader} from "./components/TestResultLoader.tsx";
import {useTabSpyLocation} from "./hooks/useTabSpyLocation.tsx";
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {getYjsDoc, syncedStore} from "@syncedstore/core";
import {WebrtcProvider} from "y-webrtc";
import {useSyncedStore} from "@syncedstore/react";
// @ts-ignore
import {MonacoBinding} from 'y-monaco'
import {useUser} from "../../store/useUser.tsx";
import ICodeEditor = editor.ICodeEditor;


type State = {
  language: {
    value: LanguageValue | undefined
  }
}

const store = syncedStore({
  language: {}
} as State);

const doc = getYjsDoc(store);
const ydocumentTextType = doc.getText('monaco')


type LanguageStore = [
  language: Language,
  setSelectedLanguage: (language: Language) => void
]

type LanguageStoreProvider = (languages: Language[]) => LanguageStore

const useSharedSelectedLanguage: LanguageStoreProvider = (languages: Language[]) => {
  const state = useSyncedStore(store)
  useEffect(() => {
    if (state.language.value === undefined) {
      state.language.value = languages[0].language
    }
  }, []);
  const setSelectedLanguage = (language: Language) => {
    state.language.value = language.language
  }
  return [
    languages.find(language => language.language === state.language.value) ?? languages[0],
    setSelectedLanguage
  ] as LanguageStore
}

const useWebrtcProvider = (online: boolean, slug: string, userId: number) => {
  const webrtcProvider = useRef<WebrtcProvider>();

  useEffect(() => {
    if (online) {
      const topic = `sync-problem-${slug}:${userId}`
      console.log('connecting to topic', topic)
      webrtcProvider.current = new WebrtcProvider(topic, doc, {
        signaling: ["ws://localhost:4444"],
      });
    }
    return () => {
      webrtcProvider.current?.disconnect();
      webrtcProvider.current = undefined;
    }
  }, [online]);

  return {
    webrtcProvider
  }
}

const useMonacoBinding = (editorRef: editor.ICodeEditor | null, webrtcProvider: MutableRefObject<WebrtcProvider | undefined>)=> {
  const monacoBinding = useRef<MonacoBinding>()
  useEffect(() => {
    if (editorRef && webrtcProvider.current) {
      // @ts-ignore
      monacoBinding.current = new MonacoBinding(ydocumentTextType, editorRef.getModel(), new Set([editorRef]), webrtcProvider.current?.awareness)
    }
    return () => {
      monacoBinding.current?.destroy()
      monacoBinding.current = undefined
    }
  }, [editorRef, webrtcProvider.current]);
  return {
    monacoBinding
  }
}

export default function Problem({host, initialOnline}: { host: boolean, initialOnline: boolean }) {
  // @ts-ignore
  console.log({host})

  const state = useSyncedStore<typeof store>(store)
  console.log({state: JSON.stringify(state)})

  const {slug} = useParams()
  const userId = host ? useUser(state => state.user?.id) : Number.parseInt(useParams().userId!)
  const [online, setOnline] = useState(initialOnline);
  const {webrtcProvider} = useWebrtcProvider(online, slug!, userId!)

  const {data} = useProblemQuery(slug!)
  const {name, description, languages, solutions} = data!
  const solved = solutions.some(solution => solution.solutionResult?.solved)
  useTabSpyLocation(name)

  const [selectedLanguage, setSelectedLanguage] = useSharedSelectedLanguage(languages)

  const selectedSolutionState = useState<Solution | null>(
    solutions && solutions.length > 0 ? solutions[0] : null
  )
  const [selectedSolution, setSelectedSolution] = selectedSolutionState

  const [editorRef, setEditorRef] = useState<ICodeEditor | null>(null)
  useMonacoBinding(editorRef, webrtcProvider);

  const [activeTab, setActiveTab] = useState<'description' | 'solutions' | null>('description')

  const testMutation = useTestMutation(slug!, selectedLanguage, {
    onMutate: () => {
      setActiveTab('solutions')
    },
    onSuccess: (response) => {
      setSelectedSolution(response.data)
      console.log(response.data)
    }
  })

  const onRunClick = () => {
    // @ts-ignore
    testMutation.mutate(editorRef?.getValue())
  }

  const editorOnMount = (editor: ICodeEditor) => {
    console.log('editor initialized', editor)
    setEditorRef(editor)
    if (editor) {
      if (host) {
        editor.setValue(selectedLanguage.template)
      }
    }
  }

  return (
    <SelectedSolutionContext.Provider value={selectedSolutionState}>
      <div className={'h-screen'}>
        <PanelGroup autoSaveId={'problem:[description-editor]'} direction={'horizontal'}>
          <Panel defaultSize={40}>
            <Flex px={16} pt={12} pb={4} align={'center'} justify={'space-between'}>
              <Flex gap={16} align={'center'}>
                <Link to={'/problem'}>
                  <IconArrowAutofitLeft size={32}/>
                </Link>
                <Title pb={4}>
                  {name}
                </Title>
                {
                  solved ? (
                    <span className={'rounded-full bg-green-200 p-2'}>
                    <IconCheck className={'text-green-600'} size={36}/>
                  </span>
                  ) : null
                }
              </Flex>
              {
                online ? (
                  <Flex gap={4}>
                    <HoverCard>
                      <HoverCard.Target>
                        <Button color={'teal'} onClick={() => setOnline(false)}>
                          Онлайн
                        </Button>
                      </HoverCard.Target>
                      <HoverCard.Dropdown>
                        <CopyButton value={`${window.location.origin}/problem/${slug}/${userId}`}>
                          {
                            ({copied, copy}) => (
                              <Button variant={'outline'} color={copied ? 'teal' : 'blue'} onClick={copy}>
                                {copied ? 'Скопирован' : 'Скопировать url'}
                              </Button>
                            )
                          }
                        </CopyButton>
                      </HoverCard.Dropdown>
                    </HoverCard>
                  </Flex>
                ) : (
                  <Button color={'gray'} onClick={() => setOnline(true)}>
                    Оффлайн
                  </Button>
                )
              }
            </Flex>
            <ProblemTabs solutions={solutions} activeTab={activeTab} setActiveTab={setActiveTab}
                         description={description}/>
          </Panel>
          <PanelResizeHandle className={'bg-slate-200 flex items-center justify-center'}>
            <IconGripVertical className={'w-[15px] text-slate-500'}/>
          </PanelResizeHandle>
          <Panel>
            <PanelGroup autoSaveId={'problem:[code-test]'} direction={'vertical'}>
              <Panel className={'pb-[60px]'} defaultSize={80}>
                <Editor onMount={editorOnMount}
                        height="100%"
                        language={selectedLanguage.language.toLowerCase()}
                />
                <Flex justify={'end'} gap={12} align={'center'} pr={20}>
                  <SegmentedControl size={'xs'} data={
                    languages.map(l => ({
                      label: <HoverCard position={'top'}>
                        <HoverCard.Target>
                          <Flex align={'center'} gap={4}>
                            {
                              {
                                Cpp: <><IconBrain/><Text size={'lg'}>C++17 gcc:13.2.0</Text></>,
                                Java: <><IconPigMoney/><Text size={'lg'}>java 21 corretto</Text></>,
                                Javascript: <><IconMoodCrazyHappy/><Text size={'lg'}>node.js 20</Text></>
                              }[l.language]
                            }
                          </Flex>
                        </HoverCard.Target>
                        {
                          selectedLanguage.language == l.language && editorRef?.getValue() !== selectedLanguage.template ? (
                            <HoverCard.Dropdown>
                              <ActionIcon variant={'light'} color={'gray'}
                                          onClick={() => editorRef?.setValue(selectedLanguage.template)}>
                                <IconRestore/>
                              </ActionIcon>
                            </HoverCard.Dropdown>
                          ) : null
                        }
                      </HoverCard>,
                      value: l.language
                    }))
                  }
                                    value={selectedLanguage.language}
                                    onChange={value => setSelectedLanguage(languages.find(l => l.language === value)!)}/>
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
