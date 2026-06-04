import {type LanguageMap, type Solution, type TestCaseResult} from "../Loader.tsx";
import {useContext, useState} from "react";
import {SelectedSolutionContext} from "../SelectedSolutionContext.tsx";
import {Badge, Button, Pill, ScrollArea, Table, Text} from "@mantine/core";
import cx from "clsx";
import classes from "../../Problems/Problems.module.css";
import {ResultPills} from "../components/components.tsx";
import {usePlatformConfigQuery} from "../../shared/PlatformConfig.ts";
import {IconChevronDown, IconChevronRight, IconCode} from "@tabler/icons-react";
import {sortedLanguageIdsByName} from "../../shared/languageSorting.ts";

const statusLabels: Record<string, string> = {
  PASSED: 'Выполнен',
  FAILED: 'Провален',
  ERROR: 'Ошибка',
  SKIPPED: 'Пропущен',
}

const statusColors: Record<string, string> = {
  PASSED: 'green',
  FAILED: 'red',
  ERROR: 'orange',
  SKIPPED: 'gray',
}

function TestCasesTable({testCases}: { testCases: TestCaseResult[] }) {
  if (!testCases.length) {
    return (
      <Text c="dimmed" size="sm" px="md" py="sm">
        Детализация тестов недоступна
      </Text>
    )
  }

  return (
    <Table className="bg-slate-50">
      <Table.Thead>
        <Table.Tr>
          <Table.Th>Тест</Table.Th>
          <Table.Th>Набор</Table.Th>
          <Table.Th>Статус</Table.Th>
          <Table.Th>Время</Table.Th>
          <Table.Th>Сообщение</Table.Th>
        </Table.Tr>
      </Table.Thead>
      <Table.Tbody>
        {testCases.map((testCase, index) => (
          <Table.Tr key={`${testCase.suiteName ?? ''}-${testCase.name ?? index}-${index}`}>
            <Table.Td>{testCase.name || `Тест ${index + 1}`}</Table.Td>
            <Table.Td>{testCase.suiteName || '—'}</Table.Td>
            <Table.Td>
              <Badge color={statusColors[testCase.status ?? ''] ?? 'gray'} variant="light">
                {statusLabels[testCase.status ?? ''] ?? testCase.status ?? 'Неизвестно'}
              </Badge>
            </Table.Td>
            <Table.Td>{testCase.durationMs == null ? '—' : `${testCase.durationMs} мс`}</Table.Td>
            <Table.Td className="max-w-[420px] whitespace-normal">
              {testCase.message || '—'}
            </Table.Td>
          </Table.Tr>
        ))}
      </Table.Tbody>
    </Table>
  )
}

export const SolutionsTable = ({
  languages,
  onLoadSolutionCode,
  loadingSolutionCodeId
}: {
  languages: LanguageMap
  onLoadSolutionCode: (solutionId: number) => void
  loadingSolutionCodeId?: number
}) => {
  const [scrolled, setScrolled] = useState(false);
  const [expandedResultIds, setExpandedResultIds] = useState<Set<string>>(new Set());
  const {data: platformConfig} = usePlatformConfigQuery()

  function Row({languageId, solution}: { languageId: string, solution: Solution }) {
    const setSelectedSolution = useContext(SelectedSolutionContext)[1]
    const resultId = `${languageId}-${solution.id}`
    const isExpanded = expandedResultIds.has(resultId)
    const testCases = solution.solutionResult?.testCases ?? []
    const toggleExpanded = () => {
      setExpandedResultIds(prev => {
        const next = new Set(prev)
        if (next.has(resultId)) {
          next.delete(resultId)
        } else {
          next.add(resultId)
        }
        return next
      })
    }

    return (
      <>
        <Table.Tr key={solution.id} className={'hover:bg-slate-50 transition-colors cursor-pointer'}
                  onClick={() => setSelectedSolution({solutionId: solution.id, languageId})}
        >
          <Table.Td>{solution.id}</Table.Td>
          <Table.Td>{platformConfig!.languages[languageId].name}</Table.Td>
          <Table.Td>
            <div className="flex flex-wrap items-center gap-2">
              {
                solution.solutionResult ? <ResultPills solutionResult={solution.solutionResult}/> : <Pill>Pending</Pill>
              }
              {solution.solutionResult && (
                <Button
                  size="xs"
                  variant="light"
                  leftSection={isExpanded ? <IconChevronDown size={14}/> : <IconChevronRight size={14}/>}
                  onClick={(event) => {
                    event.stopPropagation()
                    toggleExpanded()
                  }}
                >
                  Тесты
                </Button>
              )}
              <Button
                size="xs"
                variant="light"
                leftSection={<IconCode size={14}/>}
                loading={loadingSolutionCodeId === solution.id}
                onClick={(event) => {
                  event.stopPropagation()
                  onLoadSolutionCode(solution.id)
                }}
              >
                Код
              </Button>
            </div>
          </Table.Td>
        </Table.Tr>
        {solution.solutionResult && isExpanded && (
          <Table.Tr>
            <Table.Td colSpan={3} p={0}>
              <TestCasesTable testCases={testCases}/>
            </Table.Td>
          </Table.Tr>
        )}
      </>
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
          {sortedLanguageIdsByName(Object.keys(languages), platformConfig!.languages).flatMap(langId =>
            languages[langId].solutions.map(solution =>
              <Row key={langId + solution.id} languageId={langId} solution={solution}/>
            )
          )}
        </Table.Tbody>
      </Table>
    </ScrollArea>
  )
}
