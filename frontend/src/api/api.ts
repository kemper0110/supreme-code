import axios, {isAxiosError} from "axios";
import {keycloak} from "../keycloak.ts";


export const api = axios.create({
  withCredentials: true
})

api.interceptors.request.use((config) => {
  if (keycloak.authenticated) {
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  } else {
    console.log('interceptor: keycloak no auth!')
  }
  return config;
})

api.interceptors.response.use(undefined, (error) => {
  if (isAxiosError(error) && error.response?.status == 401) {
    console.info("401 error")
    keycloak.login()
    // useUser.getState().invalidateUser()
  }
  return Promise.reject(error)
})
