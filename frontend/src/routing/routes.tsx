import {RouteObject,} from "react-router-dom";
import Landing from "../pages/Landing/Landing.tsx";
import Page404 from "../pages/Page404";
import Page500 from "../pages/Page500";
import Auth from "../pages/Auth.tsx";
import {BaseLayout} from "../pages/BaseLayout/BaseLayout.tsx";
import {ProblemsLoader} from "../pages/Problems/Loader.tsx";
import {ProblemLoader} from "../pages/Problem/Loader.tsx";
import {Protected} from "./protection.tsx";
import {Account} from "../pages/Account/Account.tsx";
import {Support} from "../pages/Support/Support.tsx";
import {NotImplemented} from "./not-implemented.tsx";

export const routes = [
  {
    // errorElement: <ProtectedErrorBoundary/>,
    children: [
      {
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
            lazy: async () => ({
              Component: (await import("../pages/Problems/Problems.tsx")).default
            }),
            loader: ProblemsLoader
          }),
          {
            path: "/account",
            loader: NotImplemented,
            element: <Account/>
          },
          {
            path: "/support",
            loader: NotImplemented,
            element: <Support/>
          }
        ]
      },
      Protected(
        {
          path: "/problem/:slug",
          // @ts-ignore
          lazy: async () => ({
            Component: (await import("../pages/Problem/Problem.tsx")).default
          }),
          loader: ProblemLoader
        },
        {
          path: "/playground",
          lazy: async () => ({
            Component: (await import("../pages/Playground/Playground.tsx")).default
          })
        },
        {
          path: "/problem/:slug/:userId",
          lazy: async () => ({
            Component: (await import("../pages/Problem/GuestProblem.tsx")).default
          }),
          loader: ProblemLoader
        },
      )
    ]
  },
] as RouteObject[];
