import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import {routes} from "./routes.tsx";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {MantineProvider} from "@mantine/core";
import '@mantine/core/styles.css';
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";

const router = createBrowserRouter(routes)
const queryClient = new QueryClient({

})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <MantineProvider>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router}/>
      </QueryClientProvider>
    </MantineProvider>
  </React.StrictMode>,
)
