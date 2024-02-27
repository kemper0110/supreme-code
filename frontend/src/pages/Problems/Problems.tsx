import {useQuery} from "@tanstack/react-query";
import cx from 'clsx';
import {Table, ScrollArea, Skeleton} from '@mantine/core';
import classes from './Problems.module.css';
import React, {Suspense, useEffect, useState} from 'react';
import {LanguageValue} from "../../types/LanguageValue.tsx";
import {Await, useLoaderData, useNavigate} from "react-router-dom";

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
  const [scrolled, setScrolled] = useState(false);
  const navigate = useNavigate()
  const {problemsPromise} = useLoaderData()
  const onRowClick = (slug: string) => {
    navigate(`/problem/${slug}`, {
      replace: false
    })
  }

  const Rows = () => {
    const {data} = useQuery<ProblemsData>({queryKey: ['problem']})
    return data?.map(problemEntry => (
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

  const Fallback = () => {
    const [show, setShow] = useState(false)
    useEffect(() => {
      let timeout = setTimeout(() => setShow(true), 300)
      return () => {
        clearTimeout(timeout)
      }
    }, [])
    if (show)
      return Array.from({length: 5}).map((_v, i) => (
        <Table.Tr key={i} className={'hover:bg-slate-100 transition-colors cursor-pointer'}>
          {
            Array.from({length: 3}).map((_v, i) => (
              <Table.Td key={i}>
                <Skeleton width={'80%'} height={14}/>
              </Table.Td>
            ))
          }
        </Table.Tr>
      ))
    return null
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
            <Suspense fallback={<Fallback/>}>
              <Await resolve={problemsPromise}>
                <Rows/>
              </Await>
            </Suspense>
          </Table.Tbody>
        </Table>
      </ScrollArea>
    </div>
  )
}
