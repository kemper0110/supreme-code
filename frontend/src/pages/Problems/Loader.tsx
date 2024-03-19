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
}

export type ProblemWithSlug = {
  slug: string
  problem: ProblemData
}

export type ProblemsResponse = {
  problems: ProblemWithSlug[]
}

export const problemsQueryKey = () => {
  return ['problems', useUser.getState().user?.id];
};

export const ProblemsLoader = () => {
  const queryKey = problemsQueryKey()
  const problemsResponse = queryClient.getQueryData<ProblemsResponse>(queryKey)
  if (problemsResponse)
    console.log("data available")
  const problemsPromise = problemsResponse ? Promise.resolve(problemsResponse) : debouncedPromise(queryClient.fetchQuery({
    queryKey,
    queryFn: async () => (await api.get<ProblemsResponse>(`/api/problem`)).data ?? null,
    staleTime: 120000
  }), 300, 300)
  return defer({
    problemsPromise: problemsPromise
  })
}

export const useProblemsQuery = (options?: UseQueryOptions<ProblemsResponse>) =>
  useQuery<ProblemsResponse>({queryKey: problemsQueryKey(), ...options})
