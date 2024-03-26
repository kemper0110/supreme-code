import {ReactNode, useEffect, useReducer} from "react";
import {Loader} from "@mantine/core";

export const TestResultLoader = ({
                                   messages = ['Тестируем ваш код', 'Результат без перезагрузки страницы', 'Пожалуйста, подождите'],
                                   timeout = 1000
                                 }: {
  messages?: ReactNode[]
  timeout?: number
}) => {
  const [messageId, nextMessage] = useReducer((state: number) => (state + 1) % messages.length, 0)
  const message = messages[messageId]

  useEffect(() => {
    const id = setInterval(nextMessage, timeout)
    return () => clearInterval(id)
  }, []);
  return (
    <div className={'flex flex-col gap-2 items-center'}>
      <Loader/>
      <span className={'font-medium text-slate-800'}>{message}</span>
    </div>
  )
}
