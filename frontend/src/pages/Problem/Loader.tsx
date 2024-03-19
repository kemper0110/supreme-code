import {LoaderFunctionArgs} from "react-router-dom";
import {queryClient} from "../../queryClient.ts";
import {api} from "../../api/api.ts";
import {useUser} from "../../store/useUser.tsx";
import {useQuery, UseQueryOptions} from "@tanstack/react-query";
import {LanguageValue} from "../../types/LanguageValue.tsx";

export type Language = {
  id: number
  language: LanguageValue
  template: string
}

export type ProblemData = {
  id: number
  name: string
  active: boolean
  description: string
  difficulty: 'Easy' | 'Normal' | 'Hard'
  languages: Language[]
  solutions: Solution[]
}

export type Solution = {
  id: number
  code: string
  problemSlug: string
  language: string
  solutionResult?: SolutionResult
}
export type SolutionResult = {
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

export const problemQueryKey = (slug: string) =>
  ['problem', useUser.getState().user?.id, slug];


export const ProblemLoader = async (params: LoaderFunctionArgs) => {
  const slug = params.params.slug
  const queryKey = problemQueryKey(slug!)
  console.log({queryKey, params})
  return queryClient.getQueryData(queryKey) ?? await queryClient.fetchQuery({
    queryKey,
    queryFn: async () => (await api.get(`/api/problem/${encodeURIComponent(slug!)}`)).data,
    staleTime: 10000
  })
}

export const useProblemQuery = (slug: string, options?: UseQueryOptions<ProblemData>) =>
  useQuery<ProblemData>({queryKey: problemQueryKey(slug), ...options})
