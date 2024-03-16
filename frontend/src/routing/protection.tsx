import {useUser} from "../store/useUser.tsx";
import {redirect, RouteObject} from "react-router-dom";

export const protectionLoader = () => {
  const user = useUser.getState().user
  const logged = !!user

  if(!logged) {
    return redirect("/auth")
  }
  return null;
}

export const Protected = (routeObject: RouteObject, ...other: RouteObject[]): RouteObject => {
  return {
    loader: protectionLoader,
    children: [routeObject, ...other]
  }
}
