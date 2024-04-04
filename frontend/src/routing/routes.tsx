import {RouteObject,} from "react-router-dom";
import Landing from "../pages/Landing/Landing.tsx";
import Page404 from "../pages/Page404";
import Page500 from "../pages/Page500";
import Auth from "../pages/Auth.tsx";
import {BaseLayout} from "../pages/BaseLayout/BaseLayout.tsx";
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
          {
            path: "/problem",
            loader: NotImplemented,
          },
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
      {
        path: "/problem/:slug",
        loader: NotImplemented,
      },
      {
        path: "/playground",
        loader: NotImplemented,
      },
      {
        path: "/problem/:slug/:userId",
        loader: NotImplemented,
      },
    ]
  },
] as RouteObject[];
