import type {PlatformLanguage} from "../Problem/Loader.tsx";

const compareLanguageIds = (left: string, right: string) => left.localeCompare(right)

export const compareLanguageEntriesByName = (
  [leftId, leftLanguage]: [string, PlatformLanguage | undefined],
  [rightId, rightLanguage]: [string, PlatformLanguage | undefined],
) => {
  const leftName = leftLanguage?.name ?? leftId
  const rightName = rightLanguage?.name ?? rightId
  return leftName.localeCompare(rightName, undefined, {sensitivity: 'base'})
    || compareLanguageIds(leftId, rightId)
}

export const sortedLanguageEntries = (languages: Record<string, PlatformLanguage>) =>
  Object.entries(languages).sort(compareLanguageEntriesByName)

export const sortedLanguageIdsByName = (
  languageIds: string[],
  languages: Record<string, PlatformLanguage>,
) => [...languageIds].sort((leftId, rightId) =>
  compareLanguageEntriesByName(
    [leftId, languages[leftId]],
    [rightId, languages[rightId]],
  ))
