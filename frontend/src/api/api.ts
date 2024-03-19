import axios, {isAxiosError} from "axios";
import {useUser} from "../store/useUser.tsx";


export const api = axios.create({
  withCredentials: true
})


api.interceptors.response.use(undefined, (error) => {
  if(isAxiosError(error) && error.response?.status == 401) {
    console.info("401 error")
    useUser.getState().invalidateUser()
  }
  return Promise.reject(error)
})
