import {useUser} from "./store/useUser.tsx";
import {keycloak} from "./keycloak.ts";

export async function KeycloakLoader() {
  if (!keycloak.didInitialize) {
    const auth = await keycloak.init({
      onLoad: 'check-sso',
      // silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    })
    if (auth) {
      useUser.getState().setUser({
        id: keycloak.tokenParsed!.sub!,
        username: keycloak.tokenParsed!.preferred_username,
        maxAge: 100000
      }, Date.now())
    }
    return auth
  }
  return null
}
