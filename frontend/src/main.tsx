import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import {routes} from "./routing/routes.tsx";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {MantineProvider} from "@mantine/core";
import '@mantine/core/styles.css';
import '@mantine/tiptap/styles.css';
import '@mantine/charts/styles.css';
import {QueryClientProvider} from "@tanstack/react-query";
import {queryClient} from "./queryClient.ts";
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'

localStorage.log = 'true'

const router = createBrowserRouter(routes, {
  async dataStrategy({ matches }) {
    // Grab only the matches we need to run handlers for
    const matchesToLoad = matches.filter(
      (m) => m.shouldLoad
    );
    // console.log('matches to load', matchesToLoad)

    const serial = matchesToLoad.filter(match => match.route.serialLoader)
    const parallel = matchesToLoad.filter(match => !match.route.serialLoader)
    const matches2 = [...serial, ...parallel]

    const serialResults = []
    for (const match of serial) {
      serialResults.push(await match.resolve())
    }

    // Run the handlers in parallel, logging before and after
    const parallelResults = await Promise.all(
      parallel.map(async (match) => {
        console.log(`Processing ${match.route.id}`);
        // Don't override anything - just resolve route.lazy + call loader
        const result = await match.resolve();
        return result;
      })
    );

    // Aggregate the results into a bn object of `routeId -> DataStrategyResult`
    return [...serialResults, ...parallelResults].reduce(
      (acc, result, i) =>
        Object.assign(acc, {
          [matches2[i].route.id]: result,
        }),
      {}
    );
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  // <React.StrictMode>
    <MantineProvider>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router}/>
        <ReactQueryDevtools initialIsOpen={false} buttonPosition={'bottom-left'}/>
      </QueryClientProvider>
    </MantineProvider>
  // </React.StrictMode>
,
)
