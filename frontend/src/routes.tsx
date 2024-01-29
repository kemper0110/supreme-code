import {RouteObject,} from "react-router-dom";
import "./index.css";
import App from "./App.tsx";


export const routes = [
  {
    path: "/",
    element: <App/>,
  },
] as RouteObject[];
