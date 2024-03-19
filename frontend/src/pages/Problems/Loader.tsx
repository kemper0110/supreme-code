import {queryClient} from "../../queryClient.ts";
import {debouncedPromise} from "../../utils/DebouncedPromise.ts";
import {defer} from "react-router-dom";
import {api} from "../../api/api.ts";

export const ProblemsLoader = () => {
  const queryKey = ['problem']
  const problems = queryClient.getQueryData(queryKey)
  if (problems)
    console.log("data available")
  const problemsPromise = problems ? Promise.resolve(problems) : debouncedPromise(queryClient.fetchQuery({
    queryKey,
    queryFn: async () => (await api.get(`/api/problem`)).data,
    staleTime: 10000
  }), 300, 300)
  return defer({problemsPromise})
}
