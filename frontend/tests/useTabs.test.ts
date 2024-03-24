import {useTabs} from "../src/store/useTabs.tsx";
import {vi, describe, it, expect, beforeEach} from "vitest";


/**
 * Проверка работы хранилища состояния вкладок
 */
describe("useTabs", () => {
  // перед каждым тестом заново импортируем модули, чтобы хранилище было в исходном состоянии
  beforeEach(() => {
    vi.resetModules()
    useTabs.getState().reset()
    expect(useTabs.getState().tabs).toEqual([])
  })

  // вкладки можно добавлять
  it("can set tabs", () => {
    const tabs = [
      {label: "tab1", href: "/tab1"},
      {label: "tab2", href: "/tab2"}
    ]
    useTabs.getState().push(tabs[0])
    useTabs.getState().push(tabs[1])

    expect(useTabs.getState().tabs).toEqual(tabs)
  })

  // вкладки можно удалять
  it("can remove tabs", () => {
    const tabs = [
      {label: "tab1", href: "/tab1"},
      {label: "tab2", href: "/tab2"}
    ]
    useTabs.getState().push(tabs[0])
    useTabs.getState().push(tabs[1])
    useTabs.getState().remove(tabs[0].href)

    expect(useTabs.getState().tabs).toEqual([tabs[1]])
  })

  // можно повторно добавить уже имеющуюся вкладку
  // старая будет удалена, новая добавлена в конец
  it("can push tab with same href -- should replace it", () => {
    const tabs = [
      {label: "tab1", href: "/tab1"},
      {label: "tab2", href: "/tab2"},
      {label: "tab3", href: "/tab1"},
    ]

    for (const tab of tabs)
      useTabs.getState().push(tab);

    const expectedTabs = [tabs[1], tabs[2]]

    expect(useTabs.getState().tabs).toEqual(expectedTabs)
  })
})
