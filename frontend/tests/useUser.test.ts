import {describe, it, expect, beforeEach, vi} from "vitest";
import {useUser} from "../src/store/useUser";

/**
 * Проверка работы хранилища состояния аутентификации пользователя
 */
describe("useUser", () => {
  // перед каждым тестом заново импортируем модули, чтобы хранилище было в исходном состоянии
  beforeEach(() => {
    vi.resetModules()
    // состояние дополнительно сохраняется в localStorage, поэтому чистим дополнительно
    useUser.getState().reset()
    expect(useUser.getState().user).toBeNull()
  })

  // проверяем возможность установить пользователя в хранилище
  it("can set user", () => {
    const user = {id: 1, username: "test", maxAge: 1000}

    useUser.setState({user, issuedAt: Date.now()})

    expect(useUser.getState().user).toEqual(user)
  })

  // пробуем инвалидировать состояние пользователя, например при выходе из аккаунта
  it("can invalidate user", () => {
    const user = {id: 1, username: "test", maxAge: 1000}

    useUser.setState({user, issuedAt: Date.now()})

    expect(useUser.getState().user).toEqual(user)

    useUser.getState().invalidateUser()

    expect(useUser.getState().user).toBeNull()
  })

  // пользователь автоматически инвалидируется при следующем чтении из localStorage и превышении maxAge
  it("can set user with maxAge", async () => {
    const user = {id: 1, username: "test", maxAge: 0}
    useUser.setState({user, issuedAt: Date.now()})
    expect(useUser.getState().user).toEqual(user)

    await useUser.persist.rehydrate()

    const user2 = useUser.getState().user
    expect(user2).toBeNull()
  })
})
