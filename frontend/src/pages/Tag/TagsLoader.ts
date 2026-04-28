import {queryClient} from "../../queryClient.ts";
import {tagsQueryFn, tagsQueryKey} from "../shared/tags.ts";

export type Tag = {
  id: number
  name: string
}

export async function TagsLoader () {
  await queryClient.fetchQuery({
    queryKey: tagsQueryKey,
    queryFn: tagsQueryFn,
    staleTime: 100_000
  })
  return null
}


