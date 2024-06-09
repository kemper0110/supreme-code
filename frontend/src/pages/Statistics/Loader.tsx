import {queryClient} from "../../queryClient.ts";
import {api} from "../../api/api.ts";
import {useQuery, UseQueryOptions} from "@tanstack/react-query";

export type ProblemCount = {
  problemSlug: string
  count: number
}

export type LanguageCount = {
  cpp: number
  java: number
  javascript: number
}

export type DifficultyCounts = {
  easy: number
  normal: number
  hard: number
}

export type Statistics = {
  personal: {
    solvedAndAttempted: {
      attemptedCount: number
      solvedCount: number
    },
    difficultyCounts: DifficultyCounts
    languageCounts: LanguageCount
  },
  general: {
    topSolved: ProblemCount[]
    topAttempted: ProblemCount[]
    topAttemptedNotSolved: ProblemCount[]
    difficultyCounts: DifficultyCounts
    languageCounts: LanguageCount
  }
}

export const StatisticsLoader = async () => {
  const queryKey = ['statistics']
  return queryClient.getQueryData(queryKey) ?? await queryClient.fetchQuery({
    queryKey,
    queryFn: async () => (await api.get(`/api/statistics`)).data,
    staleTime: 10000
  })
}


export const useStatisticsQuery = (options?: UseQueryOptions<Statistics>) => useQuery({
  queryKey: ['statistics'],
  queryFn: async () => (await api.get(`/api/statistics`)).data,
  staleTime: 10000,
  ...options
})
