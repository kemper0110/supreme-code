import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {useEffect, useRef, useState} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {
  IconArrowAutofitLeft,
  IconBrain,
  IconCheck,
  IconCode,
  IconCoin,
  IconGripHorizontal,
  IconGripVertical,
  IconMoodCrazyHappy,
  IconRestore
} from "@tabler/icons-react";
import {
  ActionIcon,
  Button,
  CopyButton,
  Flex,
  Group,
  HoverCard,
  SegmentedControl,
  Stack,
  Tabs,
  Text,
  Title
} from "@mantine/core";
import {Link, useParams} from "react-router-dom";
import {Language, Solution, useProblemQuery, useTestMutation} from "./Loader.tsx";
import {ResultPills} from "./components/components.tsx";
import {SelectedSolutionContext} from "./SelectedSolutionContext.tsx";
import {ProblemTabs} from "./Tabs/ProblemTabs.tsx";
import {TestResultLoader} from "./components/TestResultLoader.tsx";
import {useTabSpyLocation} from "./hooks/useTabSpyLocation.tsx";
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {getYjsDoc, syncedStore} from "@syncedstore/core";
import {useSyncedStore} from "@syncedstore/react";
// @ts-ignore
import {MonacoBinding} from 'y-monaco'
import {useUser} from "../../store/useUser.tsx";
import {WebsocketProvider} from "y-websocket";
import {Awareness} from "y-protocols/awareness";
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

const useRemoteProvider = (online: boolean, slug: string, userId: number) => {
  const [status, setStatus] = useState<'connected' | 'connecting' | 'disconnected' | undefined>(undefined)
  const [synced, setSynced] = useState(false)
  // const webrtcProvider = useRef<WebrtcProvider>();
  const websocketProvider = useRef<WebsocketProvider>();

  useEffect(() => {
    if (online) {
      const topic = `sync-problem-${slug}:${userId}`
      console.log('connecting to topic', topic)
      // webrtcProvider.current = new WebrtcProvider(topic, doc, {
      //   signaling: ["ws://localhost:4444"],
      // });
      const websocketServer = 'ws://localhost:1234' //  'wss://demos.yjs.dev/ws'
      websocketProvider.current = new WebsocketProvider(websocketServer, topic, doc)

      websocketProvider.current?.on('status', (e: any) => {
        setStatus(e.status)
      })
      websocketProvider.current?.on('synced', (e: any) => {
        setSynced(e)
      })
    }
    return () => {
      console.time('disconnect')
      // webrtcProvider.current?.disconnect();
      // webrtcProvider.current = undefined;

      websocketProvider.current?.disconnect();
      websocketProvider.current = undefined;
      console.timeEnd('disconnect')
    }
  }, [online]);

  return {
    provider: websocketProvider,
    synced,
    status
    // provider: webrtcProvider
  }
}

const useMonacoBinding = (editorRef: editor.ICodeEditor | null, awareness: Awareness | undefined) => {
  const monacoBinding = useRef<MonacoBinding>()
  useEffect(() => {
    if (editorRef && awareness) {
      monacoBinding.current = new MonacoBinding(ydocumentTextType, editorRef.getModel(), new Set([editorRef]), awareness)
    }
    return () => {
      monacoBinding.current?.destroy()
      monacoBinding.current = undefined
    }
  }, [editorRef, awareness]);
  return {
    monacoBinding
  }
}

export default function Problem({host = true, initialOnline = false}: { host: boolean, initialOnline: boolean }) {
  // useEffect(() => {
  //   doc.on('update', (update) => {
  //     console.log('update', update)
  //   })
  // }, []);
  // console.log({host})

  // const state = useSyncedStore<typeof store>(store)
  // console.log({state: JSON.stringify(state)})

  const {slug} = useParams()
  const userId = host ? useUser(state => state.user?.id) : Number.parseInt(useParams().userId!)
  const [online, setOnline] = useState(initialOnline);
  const {provider, synced, status} = useRemoteProvider(online, slug!, userId!)
  console.log({synced, status})

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
  useMonacoBinding(editorRef, provider.current?.awareness);

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
    console.warn('editor initialized', editor)
    setEditorRef(editor)
    if (editor) {
      if (host) {
        editor.setValue(selectedLanguage.template)
      }
    }
  }

  return (
    <SelectedSolutionContext.Provider value={selectedSolutionState}>
      <div className={'flex flex-col px-3 pt-2 pb-4 h-screen bg-gray-100 [body:bg-gray-100] gap-1'}>
        <Flex px={'4px'} justify={'space-between'} align={'center'}>
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

          <Group justify={'center'}>
            {
              online ? (
                {
                  connected: <HoverCard>
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
                  </HoverCard>,
                  connecting: <Button color={'cyan'} onClick={() => setOnline(false)}>
                    Подключение...
                  </Button>,
                  disconnected: <Button color={'grape'} onClick={() => setOnline(false)}>
                    Отключен
                  </Button>
                }[status ?? 'connecting']
              ) : (
                <Button color={'gray'} onClick={() => setOnline(true)}>
                  Оффлайн
                </Button>
              )
            }
            <Button onClick={onRunClick}>
              Запустить
            </Button>
          </Group>
          <Flex p={'16px 16px 8px'} justify={'end'} gap={12} align={'center'} pr={20}>
            <SegmentedControl size={'xs'} data={
              languages.map(l =>
                ({
                  label: <HoverCard position={'top'} disabled={selectedLanguage.language !== l.language}>
                    <HoverCard.Target>
                      <Flex align={'center'} gap={4}>
                        {
                          {
                            Cpp: <><IconBrain/><Text size={'lg'}>C++17 gcc:13.2.0</Text></>,
                            Java: <><IconCoin/><Text size={'lg'}>Java 21 corretto</Text></>,
                            Javascript: <><IconMoodCrazyHappy/><Text size={'lg'}>node.js 20</Text></>
                          }[l.language]
                        }
                      </Flex>
                    </HoverCard.Target>
                    <HoverCard.Dropdown>
                      <ActionIcon variant={'light'} color={'gray'}
                                  onClick={() => editorRef?.setValue(selectedLanguage.template)}>
                        <IconRestore/>
                      </ActionIcon>
                    </HoverCard.Dropdown>
                  </HoverCard>,
                  value: l.language
                }))
            }
                              value={selectedLanguage.language}
                              onChange={value => setSelectedLanguage(languages.find(l => l.language === value)!)}/>
          </Flex>
        </Flex>
        <PanelGroup autoSaveId={'problem:[description-editor]'} direction={'horizontal'}>
          <Panel defaultSize={30}>
            <div className={'rounded-xl h-full'}>
              <ProblemTabs solutions={solutions} activeTab={activeTab} setActiveTab={setActiveTab}
                           description={description}/>
            </div>
          </Panel>
          <PanelResizeHandle className={'flex items-center justify-center'}>
            <IconGripVertical className={'w-[15px] text-slate-500'}/>
          </PanelResizeHandle>
          <Panel>
            <PanelGroup autoSaveId={'problem:[code-test]'} direction={'vertical'}>
              <Panel className={'rounded-xl bg-white'} defaultSize={80}>
                <Tabs value={'code'} classNames={{
                  root: "h-full"
                }}>
                  <Tabs.List className={'bg-gray-50 rounded-t-xl'}>
                    <Tabs.Tab value={'code'} leftSection={<IconCode/>}>
                      Решение
                    </Tabs.Tab>
                  </Tabs.List>
                  <Tabs.Panel value={'code'} className={'h-full'}>
                    <Editor onMount={editorOnMount}
                            loading={
                              <Text>
                                Редактор кода загружается
                              </Text>
                            }
                            height="100%"
                            language={selectedLanguage.language.toLowerCase()}
                    />
                  </Tabs.Panel>
                </Tabs>
              </Panel>
              <PanelResizeHandle className={'flex items-center justify-center'}>
                <IconGripHorizontal className={'h-[15px] text-slate-500'}/>
              </PanelResizeHandle>
              <Panel className={'!overflow-auto rounded-xl bg-white'}>
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
