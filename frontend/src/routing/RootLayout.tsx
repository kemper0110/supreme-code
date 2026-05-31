import {Outlet} from "react-router-dom";
import {SolutionResultListener} from "../components/SolutionResultListener.tsx";

export const RootLayout = () => (
  <>
    <Outlet/>
    <SolutionResultListener/>
  </>
)
