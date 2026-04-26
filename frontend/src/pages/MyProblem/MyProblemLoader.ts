import {LoaderFunctionArgs} from "react-router-dom";
import {useQuery, UseQueryOptions} from "@tanstack/react-query";
import {api} from "../../api/api.ts";
import {queryClient} from "../../queryClient.ts";
import {tagsQueryFn, tagsQueryKey} from "../shared/tags.ts";
import {fetchPlatformConfig} from "../shared/PlatformConfig.ts";

export type MyProblemView = {
  id: number
  name: string
  description: string
  difficulty: string
  languages: Record<string, MyProblemLanguageView>
  tags: number[]
}

export type MyProblemLanguageView = {
  solutionTemplate: string
  solution: string
  test: string
}

export function myProblemQueryKey(problemId: number) {
  return ['my-problem', problemId] as const
}

export async function MyProblemUpdateLoader(params: LoaderFunctionArgs) {
  const problemId = parseInt(params.params.problemId as string)
  await queryClient.fetchQuery({
    queryKey: myProblemQueryKey(problemId),
    queryFn: () => myProblemQueryFn(problemId),
    staleTime: 120_000,
  })
  await queryClient.fetchQuery({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000,
  })
  await fetchPlatformConfig()
  return null
}

export async function MyProblemCreateLoader() {
  await queryClient.fetchQuery({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000,
  })
  await fetchPlatformConfig()
  return null
}

export function useMyProblemQuery(problemId: number, options?: UseQueryOptions<MyProblemView>) {
  return useQuery({
    queryKey: myProblemQueryKey(problemId),
    queryFn: () => myProblemQueryFn(problemId),
    staleTime: 120_000,
    ...options,
  })
}

export async function myProblemQueryFn(problemId: number) {
  return (await api.get<MyProblemView>(`/api/my-problem/${problemId}`)).data ?? null
}
