import {Anchor, Button, Container, Paper, PasswordInput, Text, TextInput, Title,} from '@mantine/core';
import {upperFirst, useToggle} from '@mantine/hooks';
import {TransformedValues, useForm} from "@mantine/form";
import {GridBackground} from "../components/Background.tsx";
import {useMutation} from "@tanstack/react-query";
import {AxiosError} from "axios";
import {useNavigate} from "react-router-dom";
import {User, useUser} from "../store/useUser.tsx";
import {api} from "../api/api.ts";

type FormValues = LoginForm | RegisterForm

type LoginForm = {
  type: 'login'
  username: string
  password: string
}

type RegisterForm = {
  type: 'register'
  username: string
  password: string
  password_confirmation: string
}

type SuccessData = {
  user: User
}

// type ErrorData = {
//   field: string
//   error: string
// }

export default function Auth() {
  const navigate = useNavigate()
  const setUser = useUser(state => state.setUser)
  const [passwordVisible, togglePasswordVisible] = useToggle([false, true])
  const form = useForm<FormValues>({
    initialValues: {
      type: 'login',
      username: '',
      password: '',
      password_confirmation: '',
    } as FormValues,

    validate: {
      username: (val) => {
        if(val.length < 4) return 'Имя пользователя должно содержать не менее 4 символов'
        return null
      },
      password: (val) => (val.length < 3 ? 'Пароль должен содержать не менее 3 символов' : null),
      password_confirmation: (value, values) => {
        if(values.type === 'register' && value !== values.password)
          return 'Пароли не совпадают'
        return null
      },
    }
  });

  const submitMutation = useMutation({
    mutationFn: (values: TransformedValues<typeof form>) => {
      switch (values.type) {
        case 'login':
          return api.post<SuccessData>("/api/login", {username: values.username, password: values.password})
        case 'register':
          return api.post<SuccessData>("/api/register", {username: values.username, password: values.password})
      }
    },
    onSuccess: (response) => {
      console.log({response: response.data})
      setUser(response.data.user)
      navigate('/')
    },
    onError: (error: Error | AxiosError) => {
      console.log(error)
      // if(axios.isAxiosError(error) && error.response) {
      //   console.log(error.response.data as ErrorData)
      //   form.setErrors({
      //
      //   })
      // } else {
      // }
      form.setErrors({
        request: error.message
      })
    }
  })

  const onSubmit = (values: TransformedValues<typeof form>) => {
    console.log('errors', values)
    submitMutation.mutate(values)
  }

  return (
    <GridBackground className={'py-[193px] h-[calc(100dvh-99px)]'}>
      <Container size={420} >
        <Title ta="center" className={'font-extrabold'}>
          Добро пожаловать!
        </Title>
        <Text c="dimmed" size="sm" ta="center" mt={5}>
          <Anchor component="button" type="button" c="dimmed" onClick={() => form.setFieldValue('type', prev => prev == 'login' ? 'register' : 'login')} size="xs">
            {form.values.type === 'register'
              ? 'У вас уже есть аккаунт? Войти'
              : "Нет аккаунта? Зарегистрироваться"}
          </Anchor>
        </Text>
        {/* @ts-ignore */}
        <Paper component={'form'} c={'form'} onSubmit={form.onSubmit(onSubmit)} withBorder shadow="md" p={30} mt={37} radius="md">
          <TextInput label="Имя пользователя" placeholder="Ваше имя" required
                     {...form.getInputProps('username')}
          />
          <PasswordInput label="Пароль" placeholder="Ваш пароль" required mt="md" visible={passwordVisible}
                         onVisibilityChange={togglePasswordVisible}
                         {...form.getInputProps('password')}
          />
          {
            form.values.type == 'register' ? (
              <PasswordInput autoComplete={'password_confirmation'} label="Подтвердите пароль"
                             placeholder="Ваш пароль еще раз" required mt="md" visible={passwordVisible}
                             onVisibilityChange={togglePasswordVisible}
                             {...form.getInputProps('password_confirmation')}
              />
            ) : null
          }
          <Button type={'submit'} fullWidth mt="xl">
            {upperFirst(form.values.type)}
          </Button>
          {
            form.errors.request ? (
              <Text mt={10} c={'red'}>
                Ошибка выполнения запроса: {form.errors.request}
              </Text>
            ) : null
          }
        </Paper>
      </Container>
    </GridBackground>
  )
}
