import {RouteObject,} from "react-router-dom";
import "./index.css";
import axios from "axios";
import {queryClient} from "./queryClient.ts";


export const routes = [
  {
    path: "/",
    lazy: () => import("./pages/Root")
  },
  {
    path: "/playground",
    lazy: () => import("./pages/Playground")
  },
  {
    path: "/problem/:id",
    lazy: () => import("./pages/Problem"),
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
