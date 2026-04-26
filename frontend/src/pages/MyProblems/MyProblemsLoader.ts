import {queryClient} from "../../queryClient.ts";
import {ProblemData} from "../Problems/Loader.tsx";
import {api} from "../../api/api.ts";
import {useQuery, UseQueryOptions} from "@tanstack/react-query";
import {tagsQueryFn, tagsQueryKey} from "../shared/tags.ts";
import {fetchPlatformConfig} from "../shared/PlatformConfig.ts";

export const myProblemsQueryKey = ['my-problems']

export const myProblemsQueryFn = async () => (await api.get<ProblemData[]>(`/api/my-problem`)).data ?? null

export async function MyProblemsLoader() {
  await queryClient.fetchQuery({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000,
  })
  await fetchPlatformConfig()
  await queryClient.fetchQuery({
    queryKey: myProblemsQueryKey,
    queryFn: myProblemsQueryFn,
    staleTime: 120_000
  })
  return null
}

export function useMyProblemsQuery(options?: UseQueryOptions<ProblemData[]>) {
  return useQuery({
    queryKey: myProblemsQueryKey,
    queryFn: myProblemsQueryFn,
    staleTime: 120_000,
    ...options,
  })
}
