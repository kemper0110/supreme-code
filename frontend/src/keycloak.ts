import Keycloak from 'keycloak-js';
import {useUser} from "./store/useUser.tsx";

const keycloakConfig = {
  url: 'http://localhost:8070',
  realm: 'supreme-code',
  clientId: 'frontend',
};

export const keycloak = new Keycloak(keycloakConfig);

keycloak.init({
  onLoad: 'check-sso',
  // silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  pkceMethod: 'S256',
  checkLoginIframe: false,
}).then(auth => {
  console.log('kc auth is', {auth, authenticated: keycloak.authenticated})
  if (auth) {
    useUser.getState().setUser({
      id: keycloak.tokenParsed!.sub!,
      username: keycloak.tokenParsed!.preferred_username,
      maxAge: 100000
    }, Date.now())
  }
}).catch(err => {
  console.error('Keycloak init error', err);
});

// setInterval(() => {
//   console.log('keycloak.authenticated', keycloak, keycloak.authenticated)
// }, 1000)
