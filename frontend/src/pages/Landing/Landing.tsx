import {Button, Container, Flex, Stack} from "@mantine/core";
import {useNavigate} from "react-router-dom";
import {DotBackground} from "../../components/Background.tsx";


function Landing() {
  const navigate = useNavigate()

  return (
    <div>
      <DotBackground>
        <Container size={'md'}>
          <Flex justify={'center'} align={'flex-end'} mih={'80dvh'}>
            <Stack align={'center'} className={'text-center'} pb={130}>
              <h1 className={'text-[80px]'}>
              <span
                className={'from-blue-600 via-green-500 to-indigo-400 font-medium bg-gradient-to-r bg-clip-text text-transparent'}>Достигайте мастерства</span>
                <span className={'font-medium text-slate-700'}> через вызов</span>
              </h1>
              <p className={'mt-4 text-[24px] text-slate-800'}>
                Совершенствуйте свои навыки программирования
              </p>
              <Button mt={30} size={'xl'} onClick={() => navigate('/auth')}>Начать работу</Button>
            </Stack>
          </Flex>
        </Container>
      </DotBackground>
      <DotBackground>
        <Container size={'md'}>
          <Stack mih={'80dvh'}>
            <div className={'bg-slate-300 w-full h-[400px] rounded-2xl flex'}>
              <article className={'p-8 w-1/2'}>
                <h1 className={'font-bold text-3xl'}>
                  Оттачивайте свои навыки программирования
                </h1>
                <p className={'mt-6 text-lg font-medium'}>
                  Испытайте себя в небольших упражнениях по программированию. Каждое задание разработано создателем, чтобы помочь вам усовершенствовать различные методы программирования. Освоите выбранный вами текущий язык или быстро освоите любой из 3 поддерживаемых языков программирования.
                </p>
              </article>
              <div className={'p-8 pr-0 w-1/2'}>

              </div>
            </div>
          </Stack>
        </Container>
      </DotBackground>
    </div>
  );
}

export default Landing
