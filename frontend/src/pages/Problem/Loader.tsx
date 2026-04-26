import {LoaderFunctionArgs} from "react-router-dom";
import {queryClient} from "../../queryClient.ts";
import {api} from "../../api/api.ts";
import {useUser} from "../../store/useUser.tsx";
import {useMutation, UseMutationOptions, useQuery, UseQueryOptions} from "@tanstack/react-query";
import {AxiosResponse} from "axios";
import {fetchPlatformConfig} from "../shared/PlatformConfig.ts";
import {fetchTags} from "../shared/tags.ts";


export type PlatformConfig = {
  languages: Record<string, PlatformLanguage>
}

export type PlatformLanguage = {
  name: string
  iconPath: string
  monacoLanguageId: string
  fileExtension: string
}

export type ProblemData = {
  id: number
  name: string
  description: string
  difficulty: 'Easy' | 'Normal' | 'Hard'
  languages: LanguageMap
  tags: number[]
}

export type LanguageMap = Record<string, ProblemSolveLanguage>

export type ProblemSolveLanguage = {
  solutionTemplate: string
  solutions: Solution[]
}

export type Solution = {
  id: number
  createdAt: string
  solutionResult?: SolutionResult
}
export type SolutionResult = {
  createdAt: string
  exitCode: number
  total: number
  failures: number
  errors: number
  solved: boolean
}

export const problemQueryKey = (slug: string) =>
  ['problem', useUser.getState().user?.id, slug];


export const ProblemLoader = async (params: LoaderFunctionArgs) => {
  const slug = params.params.slug
  const queryKey = problemQueryKey(slug!)
  return await Promise.all([
    queryClient.fetchQuery({
      queryKey,
      queryFn: async () => (await api.get(`/api/problem/${encodeURIComponent(slug!)}`)).data,
      staleTime: 10_000
    }),
    fetchPlatformConfig(),
    fetchTags()
  ])
}

export const useProblemQuery = (slug: string, options?: UseQueryOptions<ProblemData>) =>
  useQuery<ProblemData>({queryKey: problemQueryKey(slug), ...options})

export const useTestMutation = (slug: string, selectedLanguage: string, options?: UseMutationOptions<AxiosResponse<Solution>, unknown, unknown, any>) => {
  return useMutation({
    ...options,
    mutationFn: (code: string) => api.post<Solution>(`/api/problem/${encodeURIComponent(slug!)}`, {
      language: selectedLanguage,
      code
    }),
    onSuccess: response => {
      const key = problemQueryKey(slug!)
      queryClient.invalidateQueries({queryKey: key})
      options?.onSuccess?.(response, undefined, undefined)
    },
  })
}
