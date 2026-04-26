import {Pill, PillGroup} from "@mantine/core";
import {SolutionResult} from "../Loader.tsx";


export const ResultPills = ({solutionResult}: { solutionResult: SolutionResult }) => {
  return (
    <PillGroup>
      <Pill>
        код завершения {solutionResult.exitCode}
      </Pill>
      <Pill>
        {solutionResult.total} тестов
      </Pill>
      <Pill>
        {solutionResult.errors} ошибок
      </Pill>
      <Pill>
        {solutionResult.failures} сбоев
      </Pill>
      <Pill fw={'bold'} className={solutionResult.solved ? '!text-green-700' : '!text-red-700'}>
        {solutionResult.solved ? 'Решена' : 'Не решена'}
      </Pill>
    </PillGroup>
  )
}


