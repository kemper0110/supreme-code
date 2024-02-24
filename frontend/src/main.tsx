import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import {routes} from "./routes.tsx";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {MantineProvider} from "@mantine/core";
import '@mantine/core/styles.css';
import '@mantine/tiptap/styles.css';
import {QueryClientProvider} from "@tanstack/react-query";
import {queryClient} from "./queryClient.ts";

const router = createBrowserRouter(routes)

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <MantineProvider>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router}/>
      </QueryClientProvider>
    </MantineProvider>
  </React.StrictMode>,
)
