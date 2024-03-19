import axios from "axios";
import {useUser} from "../store/useUser.tsx";


export const api = axios.create({
  withCredentials: true
})


api.interceptors.response.use((response) => {
  if(response.status == 401) {
    console.info("401 response")
    useUser.getState().invalidateUser()
  }
  return response
}, (error) => {
  return error
})
