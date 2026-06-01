import {Link, useParams} from "react-router-dom";
import {MyProblemLanguageView, MyProblemView, useMyProblemQuery} from "./MyProblemLoader.ts";
import {useState} from "react";
import {ActionIcon, Button, HoverCard, List, MultiSelect, Paper, Tabs, Text, TextInput} from "@mantine/core";
import {useTags} from "../shared/tags.ts";
import {Editor, loader} from "@monaco-editor/react";
import * as monaco from 'monaco-editor';
import {usePlatformConfigQuery} from "../shared/PlatformConfig.ts";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {api} from "../../api/api.ts";
import {IconArrowAutofitLeft, IconPlus, IconX} from "@tabler/icons-react";
import {myProblemsQueryKey} from "../MyProblems/MyProblemsLoader.ts";
import {hasPrivilege} from "../../auth/privileges.ts";

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

  const [directory, setDirectory] = useState<FileSystemDirectoryHandle | null>(null)

  const [state, setState] = useState(data)
  const queryClient = useQueryClient()
  const canReadMyProblems = hasPrivilege("my-problem:read")
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
  // Object.keys(state.languages)[0]
  const [tab, setTab] = useState<string | null>(null)
  const onTabChange = (value: string | null) => {
    if (value === '0') return
    setTab(value)
  };

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

  const onExport = async () => {
    async function writeFile(directory: FileSystemDirectoryHandle, path: string, content: string) {
      const file = await directory.getFileHandle(path, { create: true })
      const writable = await file.createWritable({keepExistingData: false})
      await writable.write(content)
      await writable.close()
    }

    await writeFile(directory!, 'description.md', state.description)
    await writeFile(directory!, 'info.json', JSON.stringify({
      name: state.name,
      tags: state.tags.map(id => tags!.find(t => t.id === id)?.name).filter(Boolean),
    }, null, 2))
    const languagesDir = await directory!.getDirectoryHandle('languages', {create: true})
    for (const langId in state.languages) {
      const lang = state.languages[langId]
      const langDir = await languagesDir.getDirectoryHandle(langId, {create: true})
      await writeFile(langDir, 'solution.txt', lang.solution)
      await writeFile(langDir, 'solutionTemplate.txt', lang.solutionTemplate)
      await writeFile(langDir, 'test.txt', lang.test)
    }
  }
  const onImport = async () => {
    async function readFile(directory: FileSystemDirectoryHandle, path: string) {
      const fileHandle = await directory!.getFileHandle(path, {create: false})
      const file = await fileHandle.getFile()
      return await file.text()
    }

    const description = await readFile(directory!, 'description.md').catch(() => '')
    const info = JSON.parse(await readFile(directory!, 'info.json').catch(() => '{}'))

    const languagesDir = await directory!.getDirectoryHandle('languages', {create: false}).catch<Error>(e => e)
    if (languagesDir instanceof Error)
      throw languagesDir

    const languages: Record<string, MyProblemLanguageView> = {}
    for await (const entry of languagesDir.values()) {
      console.log(entry.kind, entry.name);
      if (entry.kind !== 'directory') continue;
      const langId = entry.name
      if (!platformConfig!.languages[langId]) {
        throw new Error(`Unknown language "${langId}"`)
      }
      const langDir = await languagesDir.getDirectoryHandle(langId, {create: false})
      languages[langId] = {
        solution: await readFile(langDir, 'solution.txt').catch(() => ''),
        solutionTemplate: await readFile(langDir, 'solutionTemplate.txt').catch(() => ''),
        test: await readFile(langDir, 'test.txt').catch(() => ''),
      }
    }
    setState(p => ({
      ...p,
      description,
      name: info.name ?? p.name,
      tags: info.tags?.map((tagName: string) => tags!.find(t => t.name === tagName)?.id).filter(Boolean) ?? p.tags,
      languages,
    }))
  }

  return (
    <div className={'p-4'}>
      <div className={'flex justify-between'}>
        <div className={'flex gap-2'}>
          {canReadMyProblems && (
            <Link to={'/my-problem'}>
              <IconArrowAutofitLeft size={32}/>
            </Link>
          )}
          <Text size={'xl'}>
            Добавление задачи
          </Text>
        </div>
        <div className={'flex gap-3'}>
          <Button onClick={async () => {
            const dirHandle: FileSystemDirectoryHandle = await window.showDirectoryPicker();
            setDirectory(dirHandle)
          }}>
            Подключить директорию
          </Button>
          {
            directory ? (
              <>
                <Button onClick={onExport}>
                  Экспорт
                </Button>
                <Button onClick={onImport}>
                  Импорт
                </Button>
              </>
            ) : null
          }
          <Button onClick={() => {
          }}>
            Проверить
          </Button>
          <Button loading={save.isPending} onClick={() => {
            save.mutate()
          }}>
            Сохранить
          </Button>
        </div>
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
                  language={'markdown'}
                  onChange={v => {
                    console.log('change')
                    if (v != null)
                      setState(state => ({...state, description: v}))
                  }}
          />
        </Paper>
      </div>

      <Paper withBorder className={'p-4 mt-4'}>
        <Tabs className={'mt-2'} value={tab} onChange={onTabChange}>
          <Tabs.List>
            {
              Object.entries(state.languages).map(([id]) => (
                <Tabs.Tab key={id} value={id} rightSection={
                  <ActionIcon variant={'light'} onClick={() => deleteLang(id)}>
                    <IconX/>
                  </ActionIcon>
                }>
                  <div className={'flex gap-2 items-center'}>
                    <span>{platformConfig!.languages[id].name}</span>
                  </div>
                </Tabs.Tab>
              ))
            }
            <Tabs.Tab value={'0'}>
              <HoverCard shadow="md">
                <HoverCard.Target>
                  <IconPlus/>
                </HoverCard.Target>
                <HoverCard.Dropdown>
                  <List>
                    {
                      Object.entries(platformConfig!.languages)
                        .filter(([id]) => !(id in state.languages))
                        .map(([id, lang]) => (
                          <List.Item key={id} onClick={() => addLang(id)}>
                            <span>{lang.name}</span>
                          </List.Item>
                        ))
                    }
                  </List>
                </HoverCard.Dropdown>
              </HoverCard>
            </Tabs.Tab>
          </Tabs.List>
          {
            Object.entries(state.languages).map(([id, lang]) => (
              <Tabs.Panel key={id} value={id}>
                <Tabs defaultValue={'test'}>
                  <Tabs.List>
                    <Tabs.Tab value={'test'}>
                      Тесты
                    </Tabs.Tab>
                    <Tabs.Tab value={'solution'}>
                      Решение
                    </Tabs.Tab>
                    <Tabs.Tab value={'solutionTemplate'}>
                      Шаблон решения
                    </Tabs.Tab>
                  </Tabs.List>
                  <Tabs.Panel value={'test'} className={'h-[800px] w-full'}>
                    <Editor className={'h-full'} language={platformConfig!.languages[id].monacoLanguageId}
                            value={lang.test} onChange={v => {
                      if (v != null)
                        setLangText(id, 'test', v)
                    }}/>
                  </Tabs.Panel>
                  <Tabs.Panel value={'solution'} className={'h-[800px] w-full'}>
                    <Editor value={lang.solution} language={platformConfig!.languages[id].monacoLanguageId}
                            onChange={v => {
                              if (v != null) {
                                setLangText(id, 'solution', v)
                              }
                            }}/>
                  </Tabs.Panel>
                  <Tabs.Panel value={'solutionTemplate'} className={'h-[800px] w-full'}>
                    <Editor value={lang.solutionTemplate} language={platformConfig!.languages[id].monacoLanguageId}
                            onChange={v => {
                              if (v != null) {
                                setLangText(id, 'solutionTemplate', v)
                              }
                            }}/>
                  </Tabs.Panel>
                </Tabs>
              </Tabs.Panel>
            ))
          }
        </Tabs>
      </Paper>

    </div>
  )
}
