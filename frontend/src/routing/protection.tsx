import {useUser} from "../store/useUser.tsx";
import {redirect, RouteObject, useNavigate, useRouteError} from "react-router-dom";
import {isAxiosError} from "axios";
import {useEffect} from "react";

export const protectionLoader = () => {
  const user = useUser.getState().user
  const logged = !!user

  if (!logged) {
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


export const ProtectedErrorBoundary = () => {
  const navigate = useNavigate()
  const error = useRouteError()


  console.log("error in protected boundary", error)
  if (isAxiosError(error)) {
    if (error.response?.status == 401) {
      console.log('its 401')
      navigate("/auth")
      return null
    }
  }

  throw error
}
