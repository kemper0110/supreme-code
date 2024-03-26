import {Pill, PillGroup} from "@mantine/core";
import {SolutionResult} from "../Loader.tsx";


export const ResultPills = ({solutionResult}: { solutionResult: SolutionResult }) => {
  return (
    <PillGroup>
      <Pill>
        статус код {solutionResult.statusCode}
      </Pill>
      <Pill>
        {solutionResult.tests} тестов
      </Pill>
      <Pill>
        {solutionResult.errors} ошибок
      </Pill>
      <Pill>
        {solutionResult.failures} сбоев
      </Pill>
      <Pill>
        время {solutionResult.time}с
      </Pill>
      <Pill fw={'bold'} className={solutionResult.solved ? '!text-green-700' : '!text-red-700'}>
        {solutionResult.solved ? 'Решена' : 'Не решена'}
      </Pill>
    </PillGroup>
  )
}


