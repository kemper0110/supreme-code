import {Solution} from "../Loader.tsx";
import {Tabs} from "@mantine/core";
import {IconFileDescription, IconReport} from "@tabler/icons-react";
import {Description} from "./Description.tsx";
import {SolutionsTable} from "./SolutionsTable.tsx";

export const ProblemTabs = ({activeTab, setActiveTab, description, solutions}: {
  activeTab: string | null,
  setActiveTab: (value: 'description' | 'solutions' | null) => void,
  description: string,
  solutions: Solution[]
}) => {
  return (
    // @ts-ignore
    <Tabs value={activeTab} onChange={setActiveTab}>
      <Tabs.List className={'bg-gray-50 rounded-t-xl'}>
        <Tabs.Tab value="description" leftSection={<IconFileDescription/>}>
          Описание
        </Tabs.Tab>
        <Tabs.Tab value="solutions" leftSection={<IconReport/>}>
          Решения
        </Tabs.Tab>
      </Tabs.List>

      <Tabs.Panel className={'bg-white rounded-b-xl'} value="description">
        <Description description={description}/>
      </Tabs.Panel>

      <Tabs.Panel className={'bg-white rounded-b-xl'} value="solutions">
        <SolutionsTable solutions={solutions}/>
      </Tabs.Panel>
    </Tabs>
  )
}
