import {useQuery, UseQueryOptions} from "@tanstack/react-query";
import {PlatformConfig} from "../Problem/Loader.tsx";
import {queryClient} from "../../queryClient.ts";
import {api} from "../../api/api.ts";

export const platformConfigQueryKey = ['platform-config'] as const
export const platformConfigQueryFn = async () => (await api.get(`/api/config`)).data

export const usePlatformConfigQuery = (options?: UseQueryOptions<PlatformConfig>) =>
  useQuery<PlatformConfig>({
    queryKey: platformConfigQueryKey,
    queryFn: platformConfigQueryFn,
    staleTime: 100_000,
    ...options
  })

export function fetchPlatformConfig() {
  return queryClient.fetchQuery({
    queryKey: platformConfigQueryKey,
    queryFn: platformConfigQueryFn,
    staleTime: 100_000
  })
}
