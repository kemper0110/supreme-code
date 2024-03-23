import {describe, it, expect, beforeEach, vi} from "vitest";
import {useUser} from "../src/store/useUser";


describe("useUser", () => {
  beforeEach(() => {
    vi.resetModules()
    // user is persisted in localStorage
    useUser.getState().invalidateUser()
  })

  it("can set user", () => {
    expect(useUser.getState().user).toBeNull()

    const user = {id: 1, username: "test", maxAge: 1000}

    useUser.setState({user, issuedAt: Date.now()})

    expect(useUser.getState().user).toEqual(user)
  })

  it("can invalidate user", () => {
    expect(useUser.getState().user).toBeNull()

    const user = {id: 1, username: "test", maxAge: 1000}

    useUser.setState({user, issuedAt: Date.now()})

    expect(useUser.getState().user).toEqual(user)

    useUser.getState().invalidateUser()

    expect(useUser.getState().user).toBeNull()
  })

  it("can set user with maxAge", async () => {
    expect(useUser.getState().user).toBeNull()

    const user = {id: 1, username: "test", maxAge: 0}
    useUser.setState({user, issuedAt: Date.now()})
    expect(useUser.getState().user).toEqual(user)

    await useUser.persist.rehydrate()

    const user2 = useUser.getState().user
    expect(user2).toBeNull()
  })
})
