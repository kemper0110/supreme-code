import {keycloak} from "../keycloak.ts";

const BACKEND_CLIENT_ID = "backend";

export type Privilege =
  | "my-problem:create"
  | "my-problem:read"
  | "my-problem:update"
  | "my-problem:delete"
  | "problem:view"
  | "problem:list"
  | "solution:view"
  | "solution:submit"
  | "tag:create"
  | "tag:read"
  | "tag:update"
  | "tag:delete"
  | "platform-config:read";

type TokenWithResourceAccess = {
  resource_access?: Record<string, {roles?: string[]}>;
};

export function hasPrivilege(privilege: Privilege): boolean {
  const token = keycloak.tokenParsed as TokenWithResourceAccess | undefined;

  return token?.resource_access?.[BACKEND_CLIENT_ID]?.roles?.includes(privilege) ?? false;
}

export function hasAnyPrivilege(privileges: Privilege[]): boolean {
  return privileges.some(hasPrivilege);
}

export function hasAllPrivileges(privileges: Privilege[]): boolean {
  return privileges.every(hasPrivilege);
}
