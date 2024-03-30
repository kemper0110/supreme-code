import {Solution} from "../Loader.tsx";
import {useContext, useState} from "react";
import {SelectedSolutionContext} from "../SelectedSolutionContext.tsx";
import {Pill, ScrollArea, Table} from "@mantine/core";
import cx from "clsx";
import classes from "../../Problems/Problems.module.css";
import {ResultPills} from "../components/components.tsx";

export const SolutionsTable = ({solutions}: { solutions: Solution[] }) => {
  const [scrolled, setScrolled] = useState(false);

  function Row({solution}: { solution: Solution }) {
    const setSelectedSolution = useContext(SelectedSolutionContext)[1]
    return (
      <Table.Tr key={solution.id} className={'hover:bg-slate-50 transition-colors cursor-pointer'}
                onClick={() => setSelectedSolution(solution)}
      >
        <Table.Td>{solution.id}</Table.Td>
        <Table.Td>{solution.language}</Table.Td>
        <Table.Td>
          {
            solution.solutionResult ? <ResultPills solutionResult={solution.solutionResult}/> : <Pill>Pending</Pill>
          }
        </Table.Td>
      </Table.Tr>
    )
  }

  return (
    <ScrollArea h={'100%'} onScrollPositionChange={({y}) => setScrolled(y !== 0)}>
      <Table>
        <Table.Thead className={cx(classes.header, {[classes.scrolled]: scrolled})}>
          <Table.Tr>
            <Table.Th>#</Table.Th>
            <Table.Th>Язык</Table.Th>
            <Table.Th>Результат</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {solutions.map(solution => <Row key={solution.id} solution={solution}/>)}
        </Table.Tbody>
      </Table>
    </ScrollArea>
  )
}
