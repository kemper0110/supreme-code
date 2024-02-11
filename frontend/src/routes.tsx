import {RouteObject,} from "react-router-dom";
import "./index.css";
import axios from "axios";
import {queryClient} from "./queryClient.ts";
import Root from "./pages/Root.tsx";
import Playground from "./pages/Playground.tsx";
import Problem from "./pages/Problem.tsx";


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
    path: "/problem/:id",
    element: <Problem/>,
    loader: async (params) => {
      const id = params.params.id
      const queryKey = ['problem', id]
      console.log({queryKey, params})
      return queryClient.fetchQuery({
        queryKey,
        queryFn: async () => (await axios.get(`/api/problem/${encodeURIComponent(id!)}`, {
          params: {
            language: 'Java'
          },
        })).data,
        staleTime: 1000
      })
    }
  }
] as RouteObject[];
