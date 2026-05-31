import {Button, Text} from "@mantine/core";
import {notifications} from "@mantine/notifications";
import {IconCircleCheck, IconCircleX} from "@tabler/icons-react";
import {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {SSE} from "sse.js";
import {keycloak} from "../keycloak.ts";
import {queryClient} from "../queryClient.ts";
import {useUser} from "../store/useUser.tsx";

type SolutionResultEvent = {
  problemId: number
  solutionId: number
  languageId: string
  solved: boolean
  statusCode: number
  total: number
  failures: number
  errors: number
}

export const SolutionResultListener = () => {
  const user = useUser(state => state.user)
  const navigate = useNavigate()

  useEffect(() => {
    if (!user || !keycloak.authenticated) {
      return
    }

    let source: SSE | undefined
    let reconnectTimer: number | undefined
    let closed = false

    function scheduleReconnect() {
      if (closed || reconnectTimer !== undefined) {
        return
      }

      const currentSource = source
      reconnectTimer = window.setTimeout(() => {
        reconnectTimer = undefined
        connect()
      }, 3000)
      currentSource?.close()
    }

    async function connect() {
      try {
        await keycloak.updateToken(30)
      } catch (error) {
        console.warn("failed to refresh token before solution result SSE connection", error)
        return
      }

      if (closed || !keycloak.token) {
        return
      }

      source = new SSE("/api/solution-results/events", {
        headers: {
          Authorization: `Bearer ${keycloak.token}`,
        },
      })

      source.addEventListener("solution-result", (event: MessageEvent) => {
        const payload = JSON.parse(event.data) as SolutionResultEvent
        queryClient.invalidateQueries({queryKey: ["problem", user.id, String(payload.problemId)]})
        const notificationId = `solution-result-${payload.solutionId}`

        notifications.show({
          id: notificationId,
          color: payload.solved ? "green" : "red",
          icon: payload.solved ? <IconCircleCheck size={20}/> : <IconCircleX size={20}/>,
          title: payload.solved ? "Задача решена" : "Задача не решена",
          message: (
            <>
              <Text size="sm">
                Проверка решения #{payload.solutionId} завершена.
              </Text>
              <Button
                mt="sm"
                size="xs"
                variant="light"
                color={payload.solved ? "green" : "red"}
                onClick={() => {
                  notifications.hide(notificationId)
                  navigate(`/problem/${payload.problemId}`)
                }}
              >
                Перейти к задаче
              </Button>
            </>
          ),
        })
      })

      source.addEventListener("error", () => {
        console.warn("solution result SSE connection failed")
        scheduleReconnect()
      })

      source.addEventListener("readystatechange", (event: { readyState: number }) => {
        if (event.readyState === source?.CLOSED) {
          scheduleReconnect()
        }
      })
    }

    connect()

    return () => {
      closed = true
      if (reconnectTimer !== undefined) {
        window.clearTimeout(reconnectTimer)
      }
      source?.close()
    }
  }, [user])

  return null
}
