import {
  TextInput,
  PasswordInput,
  Anchor,
  Paper,
  Title,
  Text,
  Container,
  Button, Stack,
} from '@mantine/core';
import {useToggle, upperFirst} from '@mantine/hooks';
import {useForm} from "@mantine/form";

export default function Auth() {
  const [type, toggle] = useToggle(['login', 'register']);
  const [object, toggleObject] = useToggle([0, 1]);
  const [passwordVisible, togglePasswordVisible] = useToggle([false, true])
  const form = useForm({
    initialValues: {
      email: '',
      name: '',
      password: '',
      terms: true,
    },

    validate: {
      email: (val) => (/^\S+@\S+$/.test(val) ? null : 'Invalid email'),
      password: (val) => (val.length <= 6 ? 'Password should include at least 6 characters' : null),
    },
  });

  return (
    <Container size={420} my={40}>
      <Title ta="center" className={'font-extrabold'}>
        Welcome back!
      </Title>
      <Text c="dimmed" size="sm" ta="center" mt={5}>
        <Anchor component="button" type="button" c="dimmed" onClick={() => toggle()} size="xs">
          {type === 'register'
            ? 'Already have an account? Login'
            : "Don't have an account? Register"}
        </Anchor>
      </Text>
      {/* @ts-ignore */}
      <Paper c={'form'} onSubmit={form.onSubmit(() => {
      })}
             withBorder shadow="md" p={30} mt={30} radius="md">
        <TextInput label="Username" placeholder="Username" required/>
        <PasswordInput label="Password" placeholder="Your password" required mt="md" visible={passwordVisible}
                       onVisibilityChange={togglePasswordVisible}/>
        {
          type == 'register' ? (
            <PasswordInput autoComplete={'password_confirmation'} label="Confirm password"
                           placeholder="Your password again" required mt="md" visible={passwordVisible}
                           onVisibilityChange={togglePasswordVisible}/>
          ) : null
        }
        <Button fullWidth mt="xl">
          {upperFirst(type)}
        </Button>
        <Button onClick={() => toggleObject()}>
          toggle
        </Button>
        <Stack>
          <div>object ? "obj" : null || {object ? "obj" : null}</div>
          <div>object && "obj" || {object && "obj"}</div>
        </Stack>
      </Paper>
    </Container>
  )
}
