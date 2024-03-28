/// <reference types="vitest" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import AllureReporter from "allure-vitest/reporter";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
    server: {
      proxy: {
          "/api": {
              target: "http://localhost:8080/",
              changeOrigin: true,
              secure: false,
              configure: (proxy, _options) => {
                  proxy.on('error', (err, _req, _res) => {
                      console.log('proxy error', err);
                  });
                  proxy.on('proxyReq', (_proxyReq, req, _res) => {
                      console.log('Sending Request to the Target:', req.method, req.url);
                  });
                  proxy.on('proxyRes', (proxyRes, req, _res) => {
                      console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
                  });
              },
          }
      }
    },
  test: {
    globals: true,
    environment: 'jsdom',
    mockReset: true,
    setupFiles: ["allure-vitest/setup"],
    reporters: ["default", new AllureReporter({
      resultsDir: "../allure-report/allure-results",
    })],
  },
})
