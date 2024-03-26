import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import {useEffect, useRef, useState} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {IconArrowAutofitLeft, IconCheck, IconGripHorizontal, IconGripVertical} from "@tabler/icons-react";
import {Button, Flex, SegmentedControl, Stack, Title} from "@mantine/core";
import {Link, useParams} from "react-router-dom";
import {CppView, JavaView, NodeView} from "../../components/LanguageView.tsx";
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

const useOnline = () => {
  const [online, setOnline] = useState(false);
  const webrtcProvider = useRef<WebrtcProvider>();
  const _setOnline = (o: boolean) => {
    if (o) {
      webrtcProvider.current = new WebrtcProvider("sync-problem", doc, {
        signaling: ["ws://localhost:4444"],
      });
    } else {
      webrtcProvider.current?.disconnect();
      webrtcProvider.current = undefined;
    }
    setOnline(o);
  }

  return {
    online, setOnline: _setOnline,
    webrtcProvider
  }
}

export default function Problem() {
  const {online, setOnline, webrtcProvider} = useOnline();

  // @ts-ignore
  const host = window.navigator.userAgentData.brands[2].brand !== "Microsoft Edge"
  console.log({host})

  const state = useSyncedStore<typeof store>(store)
  console.log({state: JSON.stringify(state)})

  const {slug} = useParams()
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
  const monacoBinding = useRef<MonacoBinding>()
  useEffect(() => {
    if (editorRef)
      /* @ts-ignore */
      monacoBinding.current = new MonacoBinding(ydocumentTextType, editorRef.getModel(), new Set([editorRef]), webrtcProvider.awareness)
    return () => monacoBinding.current?.destroy()
  }, [editorRef]);

  if (host) {
    useEffect(() => {
      if (editorRef) {
        console.log('set value!', editorRef)
        editorRef.setValue(selectedLanguage.template)
      }
    }, [selectedLanguage, editorRef])
  }

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
    testMutation.mutate(editorRef.current?.getValue())
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
                  <Button color={'teal'} onClick={() => setOnline(false)}>
                    Онлайн
                  </Button>
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
                <Editor onMount={editor => {
                  setEditorRef(editor)
                  console.log('editor initialized', editor)
                }}
                        height="100%"
                        language={selectedLanguage.language.toLowerCase()}
                />
                <Flex justify={'end'} gap={12} align={'center'} pr={20}>
                  <SegmentedControl size={'xs'} data={
                    languages.map(l => ({
                      label: {Cpp: <CppView/>, Java: <JavaView/>, Javascript: <NodeView/>}[l.language],
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
