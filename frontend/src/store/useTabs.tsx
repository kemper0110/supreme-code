import {create} from "zustand";
import {persist} from "zustand/middleware";

type Tab = {
  label: string
  href: string
}

type State = {
  tabs: Tab[]
  push: (tab: Tab) => void
  remove: (href: string) => void
  reset: () => void
}

const initialTabs: Tab[] = []

const tabsStorageKey = "tabs-storage"

export const useTabs = create(persist<State>((set) => ({
  tabs: [] as Tab[],
  push: tab => set(state => ({
    ...state,
    tabs: [
      ...state.tabs.filter(({href}) => tab.href !== href),
      tab
    ]
  })),
  remove: href => set(state => ({
    ...state,
    tabs: state.tabs.filter(tab => tab.href !== href)
  })),
  reset: () => set(() => ({tabs: initialTabs}))
}), {
  name: tabsStorageKey,
}))

window.addEventListener("storage", evt => {
  if(evt.key === tabsStorageKey)
    useTabs.persist.rehydrate()
})
