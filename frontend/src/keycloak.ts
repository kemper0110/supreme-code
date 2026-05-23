import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: 'http://localhost:8070',
  realm: 'supreme-code',
  clientId: 'frontend',
};

export const keycloak = new Keycloak(keycloakConfig);
