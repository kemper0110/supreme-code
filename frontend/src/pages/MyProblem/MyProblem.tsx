import {Link, useParams} from "react-router-dom";
import {MyProblemLanguageView, MyProblemView, useMyProblemQuery} from "./MyProblemLoader.ts";
import {useState} from "react";
import {ActionIcon, Button, MultiSelect, Paper, Select, Tabs, Text, TextInput} from "@mantine/core";
import {useTags} from "../shared/tags.ts";
import {Editor, loader} from "@monaco-editor/react";
import * as monaco from 'monaco-editor';
import {usePlatformConfigQuery} from "../shared/PlatformConfig.ts";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {api} from "../../api/api.ts";
import {IconArrowAutofitLeft, IconX} from "@tabler/icons-react";
import {myProblemsQueryKey} from "../MyProblems/MyProblemsLoader.ts";

loader.config({monaco});

loader.init().then(/* ... */);

export default function MyProblem() {
  const {problemId} = useParams()
  const {data: tags} = useTags()
  const {data: platformConfig} = usePlatformConfigQuery()
  const data = problemId ? useMyProblemQuery(Number.parseInt(problemId)).data! : {
    id: 0, // не важен
    name: '',
    tags: [],
    description: '',
    languages: {},
    difficulty: 'Easy',
  } as MyProblemView

  const [state, setState] = useState(data)
  const queryClient = useQueryClient()
  const save = useMutation({
    mutationFn: async () => api.post('/api/my-problem', {
      ...state,
      id: problemId ? problemId : null
    }),
    onSettled: () => {
      queryClient.invalidateQueries({
        queryKey: myProblemsQueryKey
      })
    }
  })

  const addLang = (id: string) => {
    setState(state => ({
      ...state,
      languages: {
        ...state.languages,
        [id]: {
          solutionTemplate: '',
          solution: '',
          test: '',
        }
      }
    }))
  }
  const setLangText = (id: string, file: keyof MyProblemLanguageView, content: string) => {
    setState(state => ({
      ...state,
      languages: {
        ...state.languages,
        [id]: {
          ...state.languages[id],
          [file]: content,
        }
      }
    }))
  }
  const deleteLang = (id: string) => {
    setState(state => {
      const {[id]: deleted, ...other} = state.languages
      return {...state, languages: other};
    })
  }

  return (
    <div className={'p-4'}>
      <div className={'flex justify-between'}>
        <div className={'flex gap-2'}>
          <Link to={'/my-problem'}>
            <IconArrowAutofitLeft size={32}/>
          </Link>
          <Text size={'xl'}>
            Добавление задачи
          </Text>
        </div>
        <Button loading={save.isPending} onClick={() => {
          save.mutate()
        }}>
          Сохранить
        </Button>
      </div>

      <div className={'mt-4 h-[400px] flex gap-4'}>
        <Paper withBorder className={'w-[400px] p-2'}>
          <TextInput label={'Название задачи'} value={state.name} onChange={e => setState(state => ({
            ...state,
            name: e.target.value
          }))}/>

          <MultiSelect label={'Теги'}
                       placeholder={'Выберите из списка'}
                       data={tags!.map(t => ({value: String(t.id), label: t.name}))}
                       value={state.tags.map(t => String(t))}
                       onChange={(tags) => {
                         setState(state => ({
                           ...state,
                           tags: tags.map(t => parseInt(t, 10)),
                         }));
                       }}
                       searchable
          />
        </Paper>
        <Paper withBorder className={'w-full h-full p-2'}>
          <Editor className={'w-full h-full'}
                  value={state.description}
                  onChange={v => {
                    console.log('change')
                    if (v)
                      setState(state => ({...state, description: v}))
                  }}
          />
        </Paper>
      </div>

      <Paper withBorder className={'p-4 mt-4'}>
        <Select placeholder={'Добавить реализацию для языка'} data={
          Object.entries(platformConfig!.languages)
            .filter(([id]) => !(id in state.languages))
            .map(([id, lang]) => ({
              value: id,
              label: lang.name
            }))
        } onChange={(id) => {
          if (id) {
            addLang(id)
          }
        }} value={null}/>
        <Tabs className={'mt-2'} defaultValue={Object.keys(state.languages)[0]}>
          <Tabs.List>
            {
              Object.entries(state.languages).map(([id]) => (
                <Tabs.Tab value={id} rightSection={
                  <ActionIcon variant={'light'} onClick={() => deleteLang(id)}>
                    <IconX />
                  </ActionIcon>
                }>
                  <div className={'flex gap-2 items-center'}>
                    <span>{platformConfig!.languages[id].name}</span>
                  </div>
                </Tabs.Tab>
              ))
            }
          </Tabs.List>
          {
            Object.entries(state.languages).map(([id, lang]) => (
              <Tabs.Panel key={id} value={id}>

                <div className={'h-[800px] w-full flex gap-4'}>

                  <div className={'w-1/2'}>
                    <Text>
                      Тесты
                    </Text>
                    <Editor className={'h-full'} value={lang.test} onChange={v => {
                      if (v) {
                        setLangText(id, 'test', v)
                      }
                    }}/>
                  </div>
                  <div className={'w-1/2 flex flex-col'}>
                    <div className={'h-1/2'}>
                      <Text>
                        Решение задачи
                      </Text>
                      <Editor value={lang.solution} onChange={v => {
                        if (v) {
                          setLangText(id, 'solution', v)
                        }
                      }}/>
                    </div>
                    <div className={'mt-6 h-1/2'}>
                      <Text>
                        Шаблон решения
                      </Text>
                      <Editor value={lang.solutionTemplate} onChange={v => {
                        if (v) {
                          setLangText(id, 'solutionTemplate', v)
                        }
                      }}/>
                    </div>
                  </div>

                </div>

              </Tabs.Panel>
            ))
          }
        </Tabs>
      </Paper>

    </div>
  )
}
