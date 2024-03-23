import {useTabs} from "../src/store/useTabs.tsx";
import {describe, it, expect} from "vitest";


describe("useTabs", () => {
  it("can set tabs", () => {
    expect(useTabs.getState().tabs).toEqual([])

    const tabs = [
      {label: "tab1", href: "/tab1"},
      {label: "tab2", href: "/tab2"}
    ]
    useTabs.getState().push(tabs[0])
    useTabs.getState().push(tabs[1])

    expect(useTabs.getState().tabs).toEqual(tabs)
  })

  it("can remove tabs", () => {
    expect(useTabs.getState().tabs).toEqual([])

    const tabs = [
      {label: "tab1", href: "/tab1"},
      {label: "tab2", href: "/tab2"}
    ]
    useTabs.getState().push(tabs[0])
    useTabs.getState().push(tabs[1])
    useTabs.getState().remove(tabs[0].href)

    expect(useTabs.getState().tabs).toEqual([tabs[1]])
  })

  it("can push tab with same href -- should replace it", () => {
    expect(useTabs.getState().tabs).toEqual([])

    const tabs = [
      {label: "tab1", href: "/tab1"},
      {label: "tab2", href: "/tab2"},
      {label: "tab3", href: "/tab1"},
    ]

    for (const tab of tabs)
      useTabs.getState().push(tab);

    const expectedTabs = [tabs[2], tabs[1]]

    expect(useTabs.getState().tabs).toEqual(expectedTabs)
  })
})
