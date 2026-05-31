import {Text} from "@mantine/core";
import {PlatformLanguage} from "../pages/Problem/Loader.tsx";

export function LanguageTitle({language}:{language: PlatformLanguage}) {
  return (
    <div className={'flex items-center gap-2'}>
      <img className={'shrink-0 w-[20px]'} src={language.iconPath} alt={language.name}/>
      <Text className={'shrink-0'} size={'lg'}>{language.name}</Text>
    </div>
  )
}
