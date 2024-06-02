import {Button, Container, Flex, Stack} from "@mantine/core";
import {useNavigate} from "react-router-dom";
import {DotBackground} from "../../components/Background.tsx";
import {useUser} from "../../store/useUser.tsx";
import playground from './assets/playground.webm'
import functionBg from './assets/function-bg.jpg'
import 'reactflow/dist/style.css';
import {ReactFlow, Node, Edge} from "reactflow";
import {IconBrandReact} from "@tabler/icons-react";
import dockerjava from './assets/dockerjava.png';

function Landing() {
  const navigate = useNavigate()

  const logged = useUser(state => !!state.user)

  const onStart = () => {
    if (logged) {
      navigate('/problem')
    } else {
      navigate('/auth')
    }
  }
  const initialNodes = [
    {
      id: 'frontend', position: {
        x: 0, y: 0
      },
      data: {
        label: <span className={'flex items-center gap-2'}>
          <IconBrandReact className={'size-10'}/>
          <span className={'text-xl font-medium text-slate-700'}>Frontend</span>
        </span>
      },
      draggable: true,
      targetPosition: 'left',
      sourcePosition: 'right'
    },
    {
      id: 'backend', position: {
        x: 250, y: 0
      },
      data: {
        label: <span className={'flex items-center gap-2'}>
          <svg className={'size-10 p-0.5'} viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
            <g fill="none" fillRule="evenodd">
            <path d="M0 0h32v32H0z"/>
            <path fill="#70AD51"
                  d="M5.466 27.993c.586.473 1.446.385 1.918-.202.475-.585.386-1.445-.2-1.92-.585-.474-1.444-.383-1.92.202-.45.555-.392 1.356.115 1.844l-.266-.234C1.972 24.762 0 20.597 0 15.978 0 7.168 7.168 0 15.98 0c4.48 0 8.53 1.857 11.435 4.836.66-.898 1.232-1.902 1.7-3.015 2.036 6.118 3.233 11.26 2.795 15.31-.592 8.274-7.508 14.83-15.93 14.83-3.912 0-7.496-1.416-10.276-3.757l-.238-.21zm23.58-4.982c4.01-5.336 1.775-13.965-.085-19.48-1.657 3.453-5.738 6.094-9.262 6.93-3.303.788-6.226.142-9.283 1.318-6.97 2.68-6.86 10.992-3.02 12.86.002 0 .23.124.227.12 0-.002 5.644-1.122 8.764-2.274 4.56-1.684 9.566-5.835 11.213-10.657-.877 5.015-5.182 9.84-9.507 12.056-2.302 1.182-4.092 1.445-7.88 2.756-.464.158-.828.314-.828.314.96-.16 1.917-.212 1.917-.212 5.393-.255 13.807 1.516 17.745-3.73z"/>
            </g>
          </svg>
          <span className={'text-xl font-medium text-slate-700'}>Backend</span>
        </span>
      },
      draggable: true,
      targetPosition: 'left',
      sourcePosition: 'right'
    },
    {
      id: 'kafka', position: {
        x: 500, y: 0
      },
      data: {
        label: <span className={'flex items-center gap-2'}>
          <svg className={'size-10'} xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 32 32"
               preserveAspectRatio="xMidYMid">
            <path
              d="M21.538 17.724a4.16 4.16 0 0 0-3.128 1.42l-1.96-1.388c.208-.573.328-1.188.328-1.832a5.35 5.35 0 0 0-.317-1.802l1.956-1.373a4.16 4.16 0 0 0 3.122 1.414 4.18 4.18 0 0 0 4.172-4.172 4.18 4.18 0 0 0-4.172-4.172 4.18 4.18 0 0 0-4.172 4.172c0 .412.062.8.174 1.185l-1.957 1.374c-.818-1.014-1.995-1.723-3.336-1.94V8.25a4.18 4.18 0 0 0 3.313-4.082A4.18 4.18 0 0 0 11.388 0a4.18 4.18 0 0 0-4.172 4.172c0 1.98 1.387 3.637 3.24 4.063v2.4C7.928 11.067 6 13.273 6 15.925c0 2.665 1.947 4.88 4.493 5.308v2.523c-1.87.4-3.276 2.08-3.276 4.072A4.18 4.18 0 0 0 11.388 32a4.18 4.18 0 0 0 4.172-4.172c0-1.993-1.405-3.66-3.276-4.072v-2.523c1.315-.22 2.47-.916 3.28-1.907l1.973 1.397a4.15 4.15 0 0 0-.171 1.173 4.18 4.18 0 0 0 4.172 4.172 4.18 4.18 0 0 0 4.172-4.172 4.18 4.18 0 0 0-4.172-4.172zm0-9.754c1.115 0 2.022.908 2.022 2.023s-.907 2.022-2.022 2.022-2.022-.907-2.022-2.022.907-2.023 2.022-2.023zM9.366 4.172c0-1.115.907-2.022 2.023-2.022s2.022.907 2.022 2.022-.907 2.022-2.022 2.022-2.023-.907-2.023-2.022zM13.41 27.83c0 1.115-.907 2.022-2.022 2.022s-2.023-.907-2.023-2.022.907-2.022 2.023-2.022 2.022.907 2.022 2.022zm-2.023-9.082c-1.556 0-2.82-1.265-2.82-2.82s1.265-2.82 2.82-2.82 2.82 1.265 2.82 2.82-1.265 2.82-2.82 2.82zm10.15 5.172c-1.115 0-2.022-.908-2.022-2.023s.907-2.022 2.022-2.022 2.022.907 2.022 2.022-.907 2.023-2.022 2.023z"/>
          </svg>
          <span className={'text-xl font-medium text-slate-700'}>Kafka</span>
        </span>
      },
      draggable: true,
      targetPosition: 'left',
      sourcePosition: 'right'
    },
    {
      id: 'tester', position: {
        x: 750, y: 0
      },
      data: {
        label: <span className={'flex items-center gap-2'}>
          <img className={'size-10'} src={dockerjava} alt={'playground'}/>
          <span className={'text-xl font-medium text-slate-700'}>Tester</span>
        </span>
      },
      draggable: true,
      targetPosition: 'left',
      sourcePosition: 'right'
    }
  ] as Node[];
  const initialEdges = [
    {id: 'frontend-backend', source: 'frontend', target: 'backend', label: 'REST API'},
    {id: 'backend-kafka', source: 'backend', target: 'kafka'},
    {id: 'kafka-tester', source: 'kafka', target: 'tester'}
  ] as Edge[];
  return (
    <div className={'relative h-full'}>
      <DotBackground>
        <Container size={'md'}>
          <Flex justify={'center'} align={'flex-end'} mih={'80dvh'}>
            <Stack align={'center'} className={'text-center'} pb={130}>
              <h1 className={'text-[80px]'}>
              <span
                className={'from-blue-600 via-green-500 to-indigo-400 font-medium bg-gradient-to-r bg-clip-text text-transparent print:text-black'}>Достигайте мастерства</span>
                <span className={'font-medium text-slate-700'}> через вызов</span>
                <span className={'font-medium !bg-clip-text !bg-cover !text-transparent print:text-black'} style={{
                  background: `url(${functionBg})`,
                }}> функции</span>
              </h1>
              <p className={'mt-4 text-[24px] text-slate-700 font-medium'}>
                Совершенствуйте свои навыки программирования
              </p>
              <Button mt={30} size={'xl'} onClick={onStart}>Начать работу</Button>
            </Stack>
          </Flex>
        </Container>
      </DotBackground>
      <DotBackground>
        <Container size={'md'}>
          <Stack mih={'80dvh'}>
            <div className={'bg-slate-300 w-full h-[400px] rounded-2xl flex'}>
              <article className={'p-8 w-1/2'}>
                <h1 className={'font-bold text-3xl text-slate-700'}>
                  Оттачивайте свои навыки программирования
                </h1>
                <p className={'mt-6 text-lg font-medium text-slate-700'}>
                  Испытайте себя в небольших упражнениях по программированию. Каждое задание разработано создателем,
                  чтобы помочь вам усовершенствовать различные методы программирования. Освоите выбранный вами текущий
                  язык или быстро освоите любой из 3 поддерживаемых языков программирования.
                </p>
              </article>
              <div className={'p-8 pr-0 w-1/2'}>

              </div>
            </div>
            <div className={'mt-8 flex justify-center'}>
              <video src={playground} height={200} className={'h-[200px]'} autoPlay muted loop/>
            </div>
            <div className={'h-[200px] w-full bg-white mb-28'}>
              <ReactFlow nodes={initialNodes} edges={initialEdges}/>
            </div>
          </Stack>
        </Container>
      </DotBackground>
      {/*<div className={'absolute top-0 right-0 h-full w-[400px]'}>*/}
      {/*  <div className={'sticky top-8 right-0'}>*/}
      {/*    <TableOfContents active={''} links={[*/}
      {/*      {*/}
      {/*        label: "CTA",*/}
      {/*        link: "#CTA",*/}
      {/*        order: 1*/}
      {/*      }*/}
      {/*    ]}/>*/}
      {/*  </div>*/}
      {/*</div>*/}
    </div>
  );
}

export default Landing
