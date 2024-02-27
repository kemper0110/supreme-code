import {useQuery} from "@tanstack/react-query";
import cx from 'clsx';
import {Table, ScrollArea} from '@mantine/core';
import classes from './Problems.module.css';
import {useState} from 'react';
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {useNavigate} from "react-router-dom";

type ProblemData = {
  id: number
  name: string
  active: boolean
  description: string
  difficulty: 'Easy' | 'Normal' | 'Hard'
  languages: LanguageValue[]
}

type ProblemWithSlug = {
  slug: string
  problem: ProblemData
}

type ProblemsData = ProblemWithSlug[]

export default function Problems() {
  const {data} = useQuery<ProblemsData>({queryKey: ['problem']})
  const [scrolled, setScrolled] = useState(false);
  const navigate = useNavigate()
  console.log({data})
  const onRowClick = (slug: string) => {
    navigate(`/problem/${slug}`, {
      replace: false
    })
  }

  return (
    <div>
      <ScrollArea h={300} onScrollPositionChange={({y}) => setScrolled(y !== 0)}>
        <Table miw={700}>
          <Table.Thead className={cx(classes.header, {[classes.scrolled]: scrolled})}>
            <Table.Tr>
              <Table.Th>Name</Table.Th>
              <Table.Th>Difficulty</Table.Th>
              <Table.Th>Languages</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {
              data?.map(problemEntry => (
                <Table.Tr key={problemEntry.slug} className={'hover:bg-slate-100 transition-colors cursor-pointer'}
                          onClick={() => onRowClick(problemEntry.slug)}
                >
                  <Table.Td>{problemEntry.problem.name}</Table.Td>
                  <Table.Td>{problemEntry.problem.difficulty}</Table.Td>
                  <Table.Td className={'flex gap-2'}>
                    {
                      problemEntry.problem.languages.map(lang => (
                        <span key={lang}>
                          {lang}
                        </span>
                      ))
                    }
                  </Table.Td>
                </Table.Tr>
              ))
            }
          </Table.Tbody>
        </Table>
      </ScrollArea>
    </div>
  )
}
