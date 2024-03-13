import {defer, RouteObject,} from "react-router-dom";
import "./index.css";
import axios from "axios";
import {queryClient} from "./queryClient.ts";
import Landing from "./pages/Landing/Landing.tsx";
import Playground from "./pages/Playground.tsx";
import Problem from "./pages/Problem/Problem.tsx";
import Page404 from "./pages/Page404";
import Page500 from "./pages/Page500";
import Auth from "./pages/Auth.tsx";
import Problems from "./pages/Problems/Problems.tsx";
import {BaseLayout} from "./pages/BaseLayout/BaseLayout.tsx";
import {debouncedPromise} from "./utils/DebouncedPromise.ts";


export const routes = [
  {
    path: "/",
    element: <BaseLayout/>,
    children: [
      {
        path: "/",
        element: <Landing/>
      },
      {
        path: "/auth",
        element: <Auth/>
      },
      {
        path: "/404",
        element: <Page404/>
      },
      {
        path: "/500",
        element: <Page500/>
      },
      {
        path: "/problem",
        element: <Problems/>,
        loader: () => {
          const queryKey = ['problem']
          const problems = queryClient.getQueryData(queryKey)
          if(problems)
            console.log("data available")
          const problemsPromise = problems ? Promise.resolve(problems) : debouncedPromise(queryClient.fetchQuery({
            queryKey,
            queryFn: async () => (await axios.get(`/api/problem`)).data,
            staleTime: 10000
          }), 300, 300)
          return defer({problemsPromise})
        }
      }
    ]
  },
  {
    path: "/playground",
    element: <Playground/>
  },
  {
    path: "/problem/:slug",
    element: <Problem/>,
    loader: async (params) => {
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
    },
  },
] as RouteObject[];
