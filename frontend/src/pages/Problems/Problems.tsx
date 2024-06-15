import {Container, MultiSelect, Select, Skeleton, TextInput} from '@mantine/core';
import {Suspense, useEffect, useState} from 'react';
import {Await, Link, useLoaderData, useNavigate} from "react-router-dom";
import {DotBackground} from "../../components/Background.tsx";
import {useProblemsQuery} from "./Loader.tsx";
import {IconBrandCpp, IconBrandNodejs, IconSearch, IconTag} from "@tabler/icons-react";


export default function Problems() {
  const navigate = useNavigate()
  const state = new URLSearchParams(window.location.search)
  const [name, difficulty, languages, tags] = [state.get('name'), state.get('difficulty'), state.getAll('languages'), state.getAll('tags')]

  console.log('state', name, difficulty, languages, tags)

  const setState = (key: string, value: any) => {
    if (Array.isArray(value)) {
      state.delete(key)
      if (value.length > 0)
        value.forEach(v => state.append(key, v))
    } else {
      state.set(key, value)
    }
    navigate(`/problem?${state.toString()}`)
  }
  const unsetState = (key: string) => {
    state.delete(key)
    navigate(`/problem?${state.toString()}`)
  }

  const {problemsPromise} = useLoaderData() as { problemsPromise: Promise<unknown> }

  const Rows = () => {
    const {data} = useProblemsQuery(window.location.search.slice(1))
    return data?.problems.map(problem => (
      <div key={problem.id} className={'p-4 pe-8 bg-slate-50 flex rounded-lg items-center gap-4'}
      >
        <div className={'w-3/4'}>
          <div className={'flex gap-2 items-start'}>
            <div className={"text-xl font-bold px-4 py-0.5 bg-slate-200 " + {
              Easy: 'text-green-500',
              Normal: 'text-yellow-600',
              Hard: 'text-red-500',
            }[problem.difficulty]} style={{
              clipPath: "polygon(15% 0%, 85% 0%, 100% 50%, 85% 100%, 15% 100%, 0% 50%)"
            }}>
              {problem.difficulty}
            </div>
            <Link to={`/problem/${problem.id}`}
                  className={'text-2xl font-semibold'}
            >
              {problem.name}
            </Link>
          </div>
          <div className={'mt-4 flex gap-2 flex-wrap items-center'}>
            <IconTag size={20} stroke={2.2} className={'text-slate-700'}/>
            {
              problem.tags.map(tagId => (
                <div key={tagId} className={'border bg-slate-200 px-1 py-0.5 font-semibold rounded-lg'}>
                  {data?.tags.find(tag => tag.id === tagId)?.name}
                </div>
              ))
            }
          </div>
        </div>
        <div className={'w-1/4'}>
          <div className={'flex flex-row-reverse gap-2'}>
            {
              problem.languages.map(lang => (
                <div key={lang} className={'bg-slate-200 p-2 flex items-center justify-center rounded-md'}>
                  {
                    {
                      Javascript: <IconBrandNodejs size={50} stroke={1.3}/>,
                      Cpp: <IconBrandCpp size={50} stroke={1.4}/>,
                      Java: <IconBrandJava/>,
                    }[lang]
                  }
                </div>
              ))
            }
          </div>
        </div>
      </div>
    ))
  }

  const TagSelect = () => {
    const {data} = useProblemsQuery(window.location.search.slice(1))
    return (
      <MultiSelect
        label={'Теги'}
        data={data?.tags.map(tag => ({
          value: tag.id,
          label: tag.name
        }))}
        placeholder={'Выберите теги'}
        value={tags ?? []}
        onChange={(e) => setState('tags', e)}
      />
    )
  }

  const Fallback = () => {
    const [show, setShow] = useState(false)
    useEffect(() => {
      let timeout = setTimeout(() => setShow(true), 500)
      return () => {
        clearTimeout(timeout)
      }
    }, [])
    if (show)
      return Array.from({length: 5}).map((_v, i) => (
        <div key={i} className={'bg-slate-50 rounded-lg'}>
          <Skeleton width={'100%'} height={110}/>
        </div>
      ))
    return null
  }


  return <DotBackground className={'py-8 h-[calc(100dvh-99px)]'}>
    <Container size={'xl'} className={'h-full p-4 mx-auto bg-white shadow-lg flex gap-4'}>
      <div className={'w-3/12'}>
        <div className={'flex flex-col gap-1.5 p-3 bg-slate-50 rounded-lg'}>
          <TextInput
            value={name ?? ''}
            onChange={(e) => setState('name', e.target.value)}
            label={'Название'}
            rightSection={<IconSearch size={20}/>}
          />
          <Select label={'Сложность'} defaultValue={'Любая'} data={[
            'Любая', 'Easy', 'Normal', 'Hard',
          ]}
                  allowDeselect={false}
                  value={difficulty ?? 'Любая'}
                  onChange={(e) => {
                    if (e === 'Любая')
                      unsetState('difficulty')
                    else
                      setState('difficulty', e)
                  }}
          />

          <MultiSelect label={'Языки'} defaultValue={['Любой']} data={[
            {value: 'Cpp', label: 'C++'},
            {value: 'Java', label: 'Java'},
            {value: 'Javascript', label: 'JavaScript'}
          ]}
                       value={languages ?? []}
                       placeholder={'Выберите языки'}
                       onChange={(e) => setState('languages', e)}
          />

          <Suspense fallback={<MultiSelect
            label={'Теги'}
            data={[]}
            placeholder={'Выберите теги'}
            value={tags ?? []}
            onChange={(e) => setState('tags', e)}
          />}>
            <Await resolve={problemsPromise}>
              <TagSelect/>
            </Await>
          </Suspense>

          <Select mt={8} label={'Статус'} data={[
            'Любой', 'Без попыток', 'Не решена', 'Решена',
          ]}
                  placeholder={'Выберите статус'}
                  allowDeselect={false}
          />
        </div>
      </div>
      <div className={'w-9/12 h-full flex flex-col gap-4 overflow-y-auto'}>
        <Suspense fallback={<Fallback/>}>
          <Await resolve={problemsPromise}>
            <Rows/>
          </Await>
        </Suspense>
      </div>
    </Container>
  </DotBackground>
}


export const IconBrandJava = () => (
  <svg fill="#000000" width="50px" height="50px" viewBox="0 0 32 32">
    <path
      d="M 17.625 3 C 19.027344 6.308594 12.597656 8.335938 12 11.09375 C 11.453125 13.625 15.808594 16.59375 15.8125 16.59375 C 15.148438 15.546875 14.664063 14.664063 14 13.03125 C 12.875 10.273438 20.855469 7.785156 17.625 3 Z M 21.875 7.59375 C 21.875 7.59375 16.253906 7.949219 15.96875 11.625 C 15.839844 13.261719 17.453125 14.121094 17.5 15.3125 C 17.539063 16.285156 16.53125 17.09375 16.53125 17.09375 C 16.53125 17.09375 18.339844 16.765625 18.90625 15.28125 C 19.53125 13.632813 17.6875 12.507813 17.875 11.1875 C 18.054688 9.925781 21.875 7.59375 21.875 7.59375 Z M 23.25 16.0625 C 22.660156 16.035156 21.996094 16.253906 21.40625 16.6875 C 22.570313 16.429688 23.5625 17.160156 23.5625 18 C 23.5625 19.882813 20.875 21.65625 20.875 21.65625 C 20.875 21.65625 25.03125 21.191406 25.03125 18.09375 C 25.03125 16.816406 24.230469 16.109375 23.25 16.0625 Z M 12.21875 16.09375 C 10.769531 16.144531 7.875 16.382813 7.875 17.5 C 7.875 19.054688 14.617188 19.175781 19.4375 18.21875 C 19.4375 18.21875 20.75 17.304688 21.09375 16.96875 C 17.933594 17.625 10.71875 17.726563 10.71875 17.15625 C 10.71875 16.632813 13.03125 16.09375 13.03125 16.09375 C 13.03125 16.09375 12.703125 16.078125 12.21875 16.09375 Z M 11.78125 18.96875 C 10.988281 18.96875 9.8125 19.585938 9.8125 20.1875 C 9.8125 21.398438 15.78125 22.328125 20.1875 20.5625 L 18.65625 19.625 C 15.667969 20.601563 10.148438 20.277344 11.78125 18.96875 Z M 12.53125 21.6875 C 11.449219 21.6875 10.75 22.371094 10.75 22.875 C 10.75 24.425781 17.214844 24.578125 19.78125 23 L 18.15625 21.9375 C 16.242188 22.761719 11.425781 22.882813 12.53125 21.6875 Z M 8.90625 23.09375 C 7.140625 23.058594 6 23.859375 6 24.53125 C 6 28.105469 24.09375 27.933594 24.09375 24.28125 C 24.09375 23.675781 23.378906 23.386719 23.125 23.25 C 24.601563 26.742188 8.34375 26.46875 8.34375 24.40625 C 8.34375 23.9375 9.546875 23.46875 10.65625 23.6875 L 9.71875 23.15625 C 9.441406 23.113281 9.160156 23.097656 8.90625 23.09375 Z M 26 25.5 C 23.25 28.160156 16.289063 29.113281 9.28125 27.46875 C 16.289063 30.398438 25.964844 28.769531 26 25.5 Z"/>
  </svg>
)
