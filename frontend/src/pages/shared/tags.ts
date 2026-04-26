import {useQuery} from "@tanstack/react-query";
import {Tag} from "../Tag/TagsLoader.ts";
import {api} from "../../api/api.ts";
import {queryClient} from "../../queryClient.ts";

export const tagsQueryFn = async () => (await api.get(`/api/tag`)).data

export const tagsQueryKey = ['tags'] as const

export function useTags() {
  return useQuery<Tag[]>({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000
  })
}

export function fetchTags() {
  return queryClient.fetchQuery({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000,
  })
}
