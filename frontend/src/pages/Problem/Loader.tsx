import {LoaderFunctionArgs} from "react-router-dom";
import {queryClient} from "../../queryClient.ts";
import axios from "axios";

export const ProblemLoader = async (params: LoaderFunctionArgs) => {
  const slug = params.params.slug
  const queryKey = ['problem', slug]
  console.log({queryKey, params})
  return queryClient.getQueryData(queryKey) ?? await queryClient.fetchQuery({
    queryKey,
    queryFn: async () => (await axios.get(`/api/problem/${encodeURIComponent(slug!)}`, {
      params: {
        language: 'Java'
      },
    })).data,
    staleTime: 10000
  })
}
