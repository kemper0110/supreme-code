import {useUser} from "../store/useUser.tsx";
import {redirect, RouteObject, useNavigate, useRouteError} from "react-router-dom";
import {isAxiosError} from "axios";
import {hasPrivilege, Privilege} from "../auth/privileges.ts";
import {keycloak} from "../keycloak.ts";

type RouteLoader = (args: any) => any;

export const protectionLoader = async () => {
  const user = useUser.getState().user
  console.log('auth protection', user, performance.now())
  const logged = !!user

  if (!logged) {
    return await keycloak.login() ?? null
  }
  return null;
}

export const privilegeProtectionLoader = async (privilege: Privilege) => {
  const result = await protectionLoader()

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
  const result = await privilegeProtectionLoader(privilege)

  if (result) {
    return result
  }

  return loader ? loader(args) : redirect('/403')
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
      keycloak.login()
      return null
    }
  }

  throw error
}
