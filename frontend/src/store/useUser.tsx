import {create} from 'zustand'
import {persist} from 'zustand/middleware'

export type User = {
  id: number
  username: string
}

type State = {
  user: User | null
  setUser: (user: User) => void
  invalidateUser: () => void
}

const initialUser = null

export const useUser = create(persist<State>(
  set => ({
    user: initialUser,
    setUser: (user) => set(() => ({user})),
    invalidateUser: () => {
      console.info("user has been invalidated")
      set(() => ({user: initialUser}))
    }
  }),
  {
    name: "user-storage"
  }
))
