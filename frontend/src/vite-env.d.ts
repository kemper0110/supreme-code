/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_LSP_WS_URL?: string
  readonly VITE_YJS_WS_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
