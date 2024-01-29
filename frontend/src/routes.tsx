import {RouteObject,} from "react-router-dom";
import "./index.css";
import Root from "./pages/Root.tsx";
import Playground from "./pages/Playground.tsx";


export const routes = [
  {
    path: "/",
    element: <Root/>,
  },
  {
    path: "/playground",
    element: <Playground/>
  }
] as RouteObject[];
