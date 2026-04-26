import {queryClient} from "../../queryClient.ts";
import {tagsQueryFn, tagsQueryKey} from "../shared/tags.ts";

export type Tag = {
  id: number
  name: string
}

export async function TagsLoader () {
  // все теги загрузить
  if (queryClient.getQueryData(tagsQueryKey))
    return Promise.resolve()
  return queryClient.fetchQuery({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000
  })
}


