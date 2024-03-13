import {Anchor, Button, Container, Paper, PasswordInput, Text, TextInput, Title,} from '@mantine/core';
import {upperFirst, useToggle} from '@mantine/hooks';
import {useForm} from "@mantine/form";
import {DotBackground, GridBackground} from "../components/Background.tsx";

export default function Auth() {
  const [type, toggle] = useToggle(['login', 'register']);
  const [passwordVisible, togglePasswordVisible] = useToggle([false, true])
  const form = useForm({
    initialValues: {
      name: '',
      password: '',
    },

    validate: {
      password: (val) => (val.length <= 6 ? 'Password should include at least 6 characters' : null),
    },
  });

  return (
    <GridBackground className={'py-[193px] h-[calc(100dvh-99px)]'}>
      <Container size={420} >
        <Title ta="center" className={'font-extrabold'}>
          Добро пожаловать!
        </Title>
        <Text c="dimmed" size="sm" ta="center" mt={5}>
          <Anchor component="button" type="button" c="dimmed" onClick={() => toggle()} size="xs">
            {type === 'register'
              ? 'У вас уже есть аккаунт? Войти'
              : "Нет аккаунта? Зарегистрироваться"}
          </Anchor>
        </Text>
        {/* @ts-ignore */}
        <Paper c={'form'} onSubmit={form.onSubmit(() => {
        })}
               withBorder shadow="md" p={30} mt={37} radius="md">
          <TextInput label="Имя пользователя" placeholder="Ваше имя" required/>
          <PasswordInput label="Пароль" placeholder="Ваш пароль" required mt="md" visible={passwordVisible}
                         onVisibilityChange={togglePasswordVisible}/>
          {
            type == 'register' ? (
              <PasswordInput autoComplete={'password_confirmation'} label="Подтвердите пароль"
                             placeholder="Ваш пароль еще раз" required mt="md" visible={passwordVisible}
                             onVisibilityChange={togglePasswordVisible}/>
            ) : null
          }
          <Button fullWidth mt="xl">
            {upperFirst(type)}
          </Button>
        </Paper>
      </Container>
    </GridBackground>
  )
}
