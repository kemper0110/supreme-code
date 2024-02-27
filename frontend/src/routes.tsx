import {RouteObject,} from "react-router-dom";
import "./index.css";
import axios from "axios";
import {queryClient} from "./queryClient.ts";
import Root from "./pages/Root.tsx";
import Playground from "./pages/Playground.tsx";
import Problem from "./pages/Problem.tsx";
import Page404 from "./pages/Page404";
import Page500 from "./pages/Page500";
import Auth from "./pages/Auth.tsx";


export const routes = [
  {
    path: "/",
    element: <Root/>
  },
  {
    path: "/playground",
    element: <Playground/>
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
    path: "/auth",
    element: <Auth/>
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
        staleTime: 1000
      })
    }
  }
] as RouteObject[];
