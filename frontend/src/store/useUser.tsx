import {create} from 'zustand'
import {persist} from 'zustand/middleware'

export type User = {
  id: number
  username: string
  maxAge: number
}

type State = {
  user: User | null
  issuedAt: number
  setUser: (user: User, issuedAt: number) => void
  invalidateUser: () => void
}

const initialUser = null

export const useUser = create(persist<State>(
  set => ({
    user: initialUser,
    issuedAt: 0,
    setUser: (user, issuedAt) => set(() => ({user, issuedAt})),
    invalidateUser: () => {
      console.info("user has been invalidated")
      set(() => ({user: initialUser}))
    }
  }),
  {
    name: "user-storage"
  }
))

useUser.persist.onFinishHydration(state => {
  if (state.user && state.issuedAt + state.user.maxAge < Date.now())
    state.invalidateUser()
})
