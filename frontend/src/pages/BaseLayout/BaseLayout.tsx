import classes from "./HeaderTabs.module.css";
import {
  Avatar,
  Burger,
  Button,
  Container,
  Group,
  Menu,
  rem,
  Tabs,
  Text,
  UnstyledButton,
  useMantineTheme
} from "@mantine/core";
import {Link, Outlet, useLocation, useNavigate} from "react-router-dom";
import {
  IconChevronDown,
  IconHeart, IconLogout,
  IconSettings,
  IconStar,
  IconSwitchHorizontal,
  IconTriangleInverted, IconX
} from "@tabler/icons-react";
import cx from "clsx";
import {useDisclosure} from "@mantine/hooks";
import {useState} from "react";
import {useTabs} from "../../store/useTabs.tsx";
import {useUser} from "../../store/useUser.tsx";




export const BaseLayout = () => {
  const theme = useMantineTheme();
  const [opened, {toggle}] = useDisclosure(false);
  const [userMenuOpened, setUserMenuOpened] = useState(false);

  const navigate = useNavigate()

  const {pathname} = useLocation()
  console.log({pathname})
  const activeTab = pathname


  const customTabs = useTabs(state => state.tabs)
  const removeTab = useTabs(state => state.remove)

  const user = useUser(state => state.user)
  const invalidateUser = useUser(state => state.invalidateUser)
  console.log({user})
  const logged = !!user

  const onLogout = () => {
    invalidateUser()
    navigate("/")
  }

  const onChangeAccount = () => {
    invalidateUser()
    navigate("/auth")
  }

  return (
    <div className={'min-h-dvh'}>
      <div className={classes.header}>
        <Container className={classes.mainSection} size="md">
          <Group justify="space-between">
            <Link to={'/'} className={'flex text-3xl font-semibold'}
                  style={{transform: "skew(-20deg, 0deg)"}}
            >
              <IconTriangleInverted size={32}/>
              <span>SupremeCode</span>
            </Link>

            <Burger opened={opened} onClick={toggle} hiddenFrom="xs" size="sm"/>

            {
              logged ? (
                <Menu
                  width={260}
                  position="bottom-end"
                  transitionProps={{transition: 'pop-top-right'}}
                  onClose={() => setUserMenuOpened(false)}
                  onOpen={() => setUserMenuOpened(true)}
                  withinPortal
                >
                  <Menu.Target>
                    <UnstyledButton
                      className={cx(classes.user, {[classes.userActive]: userMenuOpened})}
                    >
                      <Group gap={7}>
                        <Avatar src={'https://raw.githubusercontent.com/mantinedev/mantine/master/.demo/avatars/avatar-5.png'} alt={user.username} radius="xl" size={20}/>
                        <Text fw={500} size="sm" lh={1} mr={3}>
                          {user.username}
                        </Text>
                        <IconChevronDown style={{width: rem(12), height: rem(12)}} stroke={1.5}/>
                      </Group>
                    </UnstyledButton>
                  </Menu.Target>
                  <Menu.Dropdown>
                    <Menu.Item
                      leftSection={
                        <IconHeart
                          style={{width: rem(16), height: rem(16)}}
                          color={theme.colors.red[6]}
                          stroke={1.5}
                        />
                      }
                    >
                      Понравившиеся задачи
                    </Menu.Item>
                    <Menu.Item
                      leftSection={
                        <IconStar
                          style={{width: rem(16), height: rem(16)}}
                          color={theme.colors.yellow[6]}
                          stroke={1.5}
                        />
                      }
                    >
                      Сохраненные задачи
                    </Menu.Item>

                    <Menu.Label>Настройки</Menu.Label>
                    <Menu.Item
                      leftSection={
                        <IconSettings style={{width: rem(16), height: rem(16)}} stroke={1.5}/>
                      }
                    >
                      Настройки аккаунта
                    </Menu.Item>
                    <Menu.Item
                      leftSection={
                        <IconSwitchHorizontal style={{width: rem(16), height: rem(16)}} stroke={1.5}/>
                      }
                      onClick={onChangeAccount}
                    >
                      Сменить аккаунт
                    </Menu.Item>
                    <Menu.Item
                      leftSection={
                        <IconLogout style={{width: rem(16), height: rem(16)}} stroke={1.5}/>
                      }
                      onClick={onLogout}
                    >
                      Выйти из аккаунта
                    </Menu.Item>

                  </Menu.Dropdown>
                </Menu>
              ) : (
                <Button onClick={() => navigate('/auth')}>Войти</Button>
              )
            }

          </Group>
        </Container>
        <Container size="md">
          <Tabs
            value={activeTab}
            variant="outline"
            visibleFrom="sm"
            classNames={{
              root: classes.tabs,
              list: classes.tabsList,
              tab: classes.tab,
            }}
            onChange={value => navigate(value!)}
          >
            <Tabs.List>
              <Tabs.Tab value={'/'}>
                Главная
              </Tabs.Tab>
              <Tabs.Tab value={'/problem'}>
                Задачи
              </Tabs.Tab>
              <Tabs.Tab value={'/playground'}>
                Компилятор
              </Tabs.Tab>
              <Tabs.Tab value={'/stats'}>
                Статистика
              </Tabs.Tab>
              <Tabs.Tab value={'/account'}>
                Учетная запись
              </Tabs.Tab>
              {/*<Tabs.Tab value={'/support'}>*/}
              {/*  Поддержка*/}
              {/*</Tabs.Tab>*/}
              {
                customTabs.map(tab => (
                  <Tabs.Tab key={tab.href} value={tab.href} rightSection={
                    <IconX size={18} className={'text-slate-600'} onClick={e => {
                      e.stopPropagation()
                      removeTab(tab.href)
                    }}/>
                  }>
                    {tab.label}
                  </Tabs.Tab>
                ))
              }
            </Tabs.List>
          </Tabs>
        </Container>
      </div>

      <Outlet/>
    </div>
  )
}
