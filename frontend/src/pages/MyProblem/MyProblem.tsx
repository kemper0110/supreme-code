import {Link, useParams} from "react-router-dom";
import {MyProblemLanguageView, MyProblemView, useMyProblemQuery} from "./MyProblemLoader.ts";
import {useEffect, useRef, useState} from "react";
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
import type {PlatformConfig} from "../Problem/Loader.tsx";
import type {Tag} from "../Tag/TagsLoader.ts";

loader.config({monaco});

loader.init().then(/* ... */);

const mcpWebSocketUrl = 'ws://localhost:3300/frontend'

const mcpMethods = ['exportCurrentProblem', 'importCurrentProblem', 'platformInfo', 'validateCurrentProblem'] as const

type McpMethod = typeof mcpMethods[number]

type McpRequest = {
  id: string
  method: McpMethod
  params?: Record<string, unknown>
}

type McpResponse = {
  id: string
  result?: unknown
  error?: string
}

type ImportedProblem = {
  description: string
  name?: string
  tags?: number[]
  difficulty?: string
  languages: Record<string, MyProblemLanguageView>
}

const exportedFiles = (languages: Record<string, MyProblemLanguageView>) => [
  'description.md',
  'info.json',
  ...Object.keys(languages).flatMap(languageId => [
    `languages/${languageId}/solution.txt`,
    `languages/${languageId}/solutionTemplate.txt`,
    `languages/${languageId}/test.txt`,
  ]),
]

async function writeFile(directory: FileSystemDirectoryHandle, path: string, content: string) {
  const file = await directory.getFileHandle(path, {create: true})
  const writable = await file.createWritable({keepExistingData: false})
  await writable.write(content)
  await writable.close()
}

async function readFile(directory: FileSystemDirectoryHandle, path: string) {
  const fileHandle = await directory.getFileHandle(path, {create: false})
  const file = await fileHandle.getFile()
  return await file.text()
}

function requireValue<T>(value: T | null | undefined, message: string): T {
  if (value == null) {
    throw new Error(message)
  }
  return value
}

function parseInfo(text: string) {
  const value = JSON.parse(text)
  return value && typeof value === 'object' && !Array.isArray(value)
    ? value as Record<string, unknown>
    : {}
}

function tagIdsToNames(tagIds: number[], availableTags: Tag[]) {
  return tagIds
    .map(id => availableTags.find(tag => tag.id === id)?.name)
    .filter((name): name is string => name != null)
}

function tagNamesToIds(tagNames: unknown, availableTags: Tag[]) {
  if (!Array.isArray(tagNames)) {
    return undefined
  }

  return tagNames
    .map(tagName => typeof tagName === 'string'
      ? availableTags.find(tag => tag.name === tagName)?.id
      : undefined)
    .filter((id): id is number => id != null)
}

async function exportProblemToDirectory(
  directory: FileSystemDirectoryHandle,
  problem: MyProblemView,
  availableTags: Tag[],
) {
  await writeFile(directory, 'description.md', problem.description)
  await writeFile(directory, 'info.json', JSON.stringify({
    name: problem.name,
    tags: tagIdsToNames(problem.tags, availableTags),
    difficulty: problem.difficulty,
  }, null, 2))

  const languagesDir = await directory.getDirectoryHandle('languages', {create: true})
  for (const [languageId, language] of Object.entries(problem.languages)) {
    const languageDir = await languagesDir.getDirectoryHandle(languageId, {create: true})
    await writeFile(languageDir, 'solution.txt', language.solution)
    await writeFile(languageDir, 'solutionTemplate.txt', language.solutionTemplate)
    await writeFile(languageDir, 'test.txt', language.test)
  }

  return {
    directoryName: directory.name,
    files: exportedFiles(problem.languages),
  }
}

async function importProblemFromDirectory(
  directory: FileSystemDirectoryHandle,
  availableTags: Tag[],
  platformConfig: PlatformConfig,
) {
  const description = await readFile(directory, 'description.md').catch(() => '')
  const info = parseInfo(await readFile(directory, 'info.json').catch(() => '{}'))
  const languagesDir = await directory.getDirectoryHandle('languages', {create: false})
  const languages: Record<string, MyProblemLanguageView> = {}

  // @ts-ignore
  for await (const entry of languagesDir.values()) {
    if (entry.kind !== 'directory') {
      continue
    }

    const languageId = entry.name
    if (!platformConfig.languages[languageId]) {
      throw new Error(`Unknown language "${languageId}"`)
    }

    const languageDir = await languagesDir.getDirectoryHandle(languageId, {create: false})
    languages[languageId] = {
      solution: await readFile(languageDir, 'solution.txt').catch(() => ''),
      solutionTemplate: await readFile(languageDir, 'solutionTemplate.txt').catch(() => ''),
      test: await readFile(languageDir, 'test.txt').catch(() => ''),
    }
  }

  return {
    directoryName: directory.name,
    languages: Object.keys(languages),
    problem: {
      description,
      name: typeof info.name === 'string' ? info.name : undefined,
      tags: tagNamesToIds(info.tags, availableTags),
      difficulty: typeof info.difficulty === 'string' ? info.difficulty : undefined,
      languages,
    } satisfies ImportedProblem,
  }
}

function validateProblem(problem: MyProblemView, platformConfig: PlatformConfig) {
  const errors: string[] = []
  const languageIds = Object.keys(problem.languages)

  if (!problem.name.trim()) {
    errors.push('Name is required')
  }
  if (!problem.description.trim()) {
    errors.push('Description is required')
  }
  if (languageIds.length === 0) {
    errors.push('At least one language is required')
  }
  for (const languageId of languageIds) {
    if (!platformConfig.languages[languageId]) {
      errors.push(`Unknown language "${languageId}"`)
    }
  }

  return {ok: errors.length === 0, errors}
}

function isMcpRequest(value: unknown): value is McpRequest {
  if (!value || typeof value !== 'object') {
    return false
  }

  const message = value as Partial<McpRequest>
  return typeof message.id === 'string'
    && typeof message.method === 'string'
    && mcpMethods.includes(message.method as McpMethod)
}

export default function MyProblem() {
  const {problemId} = useParams()
  const {data: tags} = useTags()
  const {data: platformConfig} = usePlatformConfigQuery()
  const problemIdNumber = problemId ? Number.parseInt(problemId, 10) : null
  const problemQuery = useMyProblemQuery(problemIdNumber ?? 0, {enabled: problemIdNumber != null})
  const data = problemIdNumber != null ? problemQuery.data! : {
    id: 0, // не важен
    name: '',
    tags: [],
    description: '',
    languages: {},
    difficulty: 'Easy',
  } as MyProblemView

  const [directory, setDirectory] = useState<FileSystemDirectoryHandle | null>(null)
  const [mcpStatus, setMcpStatus] = useState<string | null>(null)

  const [state, setState] = useState(data)

  const stateRef = useRef(state)
  const tagsRef = useRef(tags)
  const platformConfigRef = useRef(platformConfig)
  const directoryRef = useRef(directory)
  const wsRef = useRef<WebSocket | null>(null)
  const handleMcpRequestRef = useRef<(message: McpRequest) => Promise<void>>(async () => undefined)
  stateRef.current = state
  tagsRef.current = tags
  platformConfigRef.current = platformConfig
  directoryRef.current = directory

  const queryClient = useQueryClient()
  const canReadMyProblems = hasPrivilege("my-problem:read")
  const save = useMutation({
    mutationFn: async () => api.post('/api/my-problem', {
      ...state,
      id: problemIdNumber
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
      const languages = {...state.languages}
      delete languages[id]
      return {...state, languages};
    })
  }

  const reportError = (error: unknown) => {
    setMcpStatus(error instanceof Error ? error.message : 'Unknown error')
  }

  const runWithStatus = (operation: () => Promise<unknown>) => {
    void operation().catch(reportError)
  }

  const exportCurrentProblem = async () => {
    const currentDirectory = requireValue(directoryRef.current, 'Connect a directory first')
    const availableTags = requireValue(tagsRef.current, 'Tags are not loaded yet')
    const result = await exportProblemToDirectory(currentDirectory, stateRef.current, availableTags)
    setMcpStatus(`Exported to ${result.directoryName}`)
    return {ok: true, ...result}
  }

  const importCurrentProblem = async () => {
    const currentDirectory = requireValue(directoryRef.current, 'Connect a directory first')
    const availableTags = requireValue(tagsRef.current, 'Tags are not loaded yet')
    const currentPlatformConfig = requireValue(platformConfigRef.current, 'Platform config is not loaded yet')
    const result = await importProblemFromDirectory(currentDirectory, availableTags, currentPlatformConfig)

    setState(previous => ({
      ...previous,
      description: result.problem.description,
      name: result.problem.name ?? previous.name,
      tags: result.problem.tags ?? previous.tags,
      difficulty: result.problem.difficulty ?? previous.difficulty,
      languages: result.problem.languages,
    }))
    setTab(result.languages[0] ?? null)
    setMcpStatus(`Imported from ${result.directoryName}`)

    return {
      ok: true,
      directoryName: result.directoryName,
      languages: result.languages,
    }
  }

  const platformInfo = () => {
    const currentTags = requireValue(tagsRef.current, 'Tags are not loaded yet')
    const currentPlatformConfig = requireValue(platformConfigRef.current, 'Platform config is not loaded yet')

    return {
      tags: currentTags.map(tag => tag.name),
      tagDetails: currentTags,
      languages: Object.keys(currentPlatformConfig.languages),
      languageDetails: currentPlatformConfig.languages,
    }
  }

  const validateCurrentProblem = () => {
    const currentPlatformConfig = requireValue(platformConfigRef.current, 'Platform config is not loaded yet')
    const result = validateProblem(stateRef.current, currentPlatformConfig)
    setMcpStatus(result.ok ? 'Validation passed' : `Validation failed: ${result.errors.join('; ')}`)
    return result
  }
  const validateFromMcp = async () => {
    await importCurrentProblem()
    return validateCurrentProblem()
  }

  const sendMcpResponse = (response: McpResponse) => {
    wsRef.current?.send(JSON.stringify(response))
  }

  const handleMcpRequest = async (message: McpRequest) => {
    try {
      const result = message.method === 'exportCurrentProblem'
        ? await exportCurrentProblem()
        : message.method === 'importCurrentProblem'
          ? await importCurrentProblem()
          : message.method === 'platformInfo'
            ? platformInfo()
            : await validateFromMcp()

      sendMcpResponse({id: message.id, result})
    } catch (error) {
      sendMcpResponse({
        id: message.id,
        error: error instanceof Error ? error.message : 'Unknown frontend error',
      })
    }
  }
  handleMcpRequestRef.current = handleMcpRequest

  useEffect(() => {
    const socket = new WebSocket(mcpWebSocketUrl)
    wsRef.current = socket
    setMcpStatus('Connecting to MCP...')
    socket.onopen = () => setMcpStatus('MCP connected')
    socket.onclose = () => {
      if (wsRef.current === socket) {
        wsRef.current = null
        setMcpStatus('MCP disconnected')
      }
    }
    socket.onerror = () => setMcpStatus('MCP connection error')
    socket.onmessage = event => {
      try {
        const message = JSON.parse(event.data) as unknown
        if (!isMcpRequest(message)) {
          throw new Error('Invalid MCP request')
        }
        void handleMcpRequestRef.current(message)
      } catch (error) {
        setMcpStatus(`MCP message failed: ${error instanceof Error ? error.message : 'unknown error'}`)
      }
    }
    return () => {
      if (wsRef.current === socket) {
        wsRef.current = null
      }
      socket.close()
    }
  }, [])

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
            // @ts-ignore
            const dirHandle: FileSystemDirectoryHandle = await window.showDirectoryPicker();
            setDirectory(dirHandle)
            setMcpStatus(`Directory connected: ${dirHandle.name}`)
          }}>
            Подключить директорию
          </Button>
          {
            directory ? (
              <>
                <Button onClick={() => runWithStatus(exportCurrentProblem)}>
                  Экспорт
                </Button>
                <Button onClick={() => runWithStatus(importCurrentProblem)}>
                  Импорт
                </Button>
              </>
            ) : null
          }
          <Button onClick={() => runWithStatus(async () => validateCurrentProblem())}>
            Проверить
          </Button>
          <Button loading={save.isPending} onClick={() => {
            save.mutate()
          }}>
            Сохранить
          </Button>
        </div>
      </div>
      {mcpStatus ? (
        <Text size={'sm'} c={'dimmed'} className={'mt-2'}>
          {mcpStatus}
        </Text>
      ) : null}

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
