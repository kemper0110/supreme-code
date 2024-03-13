import {Button, Container, Flex, Stack} from "@mantine/core";
import {useNavigate} from "react-router-dom";
import {DotBackground} from "../../components/Background.tsx";


function Landing() {
  const navigate = useNavigate()

  return (
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
  );
}

export default Landing
