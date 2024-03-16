import {RouteObject,} from "react-router-dom";
import Landing from "../pages/Landing/Landing.tsx";
import Playground from "../pages/Playground.tsx";
import Problem from "../pages/Problem/Problem.tsx";
import Page404 from "../pages/Page404";
import Page500 from "../pages/Page500";
import Auth from "../pages/Auth.tsx";
import Problems from "../pages/Problems/Problems.tsx";
import {BaseLayout} from "../pages/BaseLayout/BaseLayout.tsx";
import {ProblemsLoader} from "../pages/Problems/Loader.tsx";
import {ProblemLoader} from "../pages/Problem/Loader.tsx";
import {Protected} from "./protection.tsx";

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
      Protected({
        path: "/problem",
        element: <Problems/>,
        loader: ProblemsLoader
      }),
    ]
  },
  Protected(
    {
      path: "/problem/:slug",
      element: <Problem/>,
      loader: ProblemLoader
    },
    {
      path: "/playground",
      element: <Playground/>
    }
  )
] as RouteObject[];
