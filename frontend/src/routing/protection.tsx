import {useUser} from "../store/useUser.tsx";
import {redirect, RouteObject, useNavigate, useRouteError} from "react-router-dom";
import {isAxiosError} from "axios";
import {hasPrivilege, Privilege} from "../auth/privileges.ts";

type RouteLoader = (args: any) => any;

export const protectionLoader = () => {
  const user = useUser.getState().user
  console.log('auth protection', user, performance.now())
  const logged = !!user

  if (!logged) {
    return redirect("/auth")
  }
  return null;
}

export const privilegeProtectionLoader = (privilege: Privilege) => {
  const result = protectionLoader()

  if (result) {
    return result
  }

  if (!hasPrivilege(privilege)) {
    return redirect("/403")
  }

  return null
}

export const withPrivilege = (
  privilege: Privilege,
  loader?: RouteLoader,
): RouteLoader => async (args) => {
  const result = privilegeProtectionLoader(privilege)

  if (result) {
    return result
  }

  return loader ? loader(args) : null
}

export const Protected = (routeObject: RouteObject, ...other: RouteObject[]): RouteObject => {
  return {
    loader: protectionLoader,
    serialLoader: true,
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
