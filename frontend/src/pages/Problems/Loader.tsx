import {queryClient} from "../../queryClient.ts";
import {debouncedPromise} from "../../utils/DebouncedPromise.ts";
import {defer} from "react-router-dom";
import {api} from "../../api/api.ts";
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {useQuery, UseQueryOptions} from "@tanstack/react-query";
import {useUser} from "../../store/useUser.tsx";

export type ProblemData = {
  id: number
  name: string
  active: boolean
  description: string
  difficulty: 'Easy' | 'Normal' | 'Hard'
  languages: LanguageValue[]
  tags: string[]
}

export type ProblemsResponse = {
  problems: ProblemData[]
  tags: {
    id: string
    name: string
  }[]
}

export const problemsQueryKey = (searchParams: string) => {
  return ['problems', useUser.getState().user?.id, searchParams];
};

export const ProblemsLoader = ({request}: {request: Request}) => {
  const [,searchParams = ''] = request.url.split("?");
  // const state = new URLSearchParams(searchParams)
  // const [name, difficulty, language, tags] = [state.get('name'), state.get('difficulty'), state.getAll('language'), state.getAll('tags')]

  const queryKey = problemsQueryKey(searchParams)
  const problemsResponse = queryClient.getQueryData<ProblemsResponse>(queryKey)
  if (problemsResponse)
    console.log("data available")
  const problemsPromise = problemsResponse ? Promise.resolve(problemsResponse) : debouncedPromise(queryClient.fetchQuery({
    queryKey,
    queryFn: async () => (await api.get<ProblemsResponse>(`/api/problem?${searchParams}`)).data ?? null,
    staleTime: 120000
  }), 500, 500)
  return defer({
    problemsPromise: problemsPromise
  })
}

export const useProblemsQuery = (searchParams: string, options?: UseQueryOptions<ProblemsResponse>) =>
  useQuery<ProblemsResponse>({queryKey: problemsQueryKey(searchParams), ...options})
