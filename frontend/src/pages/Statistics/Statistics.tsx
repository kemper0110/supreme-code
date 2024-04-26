import {DotBackground} from "../../components/Background.tsx";
import {Container, Paper, Text, Title} from "@mantine/core";
import {BarChart, PieChart, RadarChart} from "@mantine/charts";
import {useStatisticsQuery, Statistics} from "./Loader.tsx";

export default function Statistics() {
  const {data} = useStatisticsQuery() as { data: Statistics }

  return (
    <DotBackground className={'pt-8 min-h-[calc(100dvh-99px)]'}>
      <Container size="md" bg={'white'} className={'h-full'}>
        <Title size={'h1'}>
          Ваша статистика
        </Title>

        <Paper mt={24} shadow="xs" p="xl">
          <Title size={'h3'}>Количество решенных и начатых задач</Title>
          <PieChart
            mt={24}
            size={250}
            data={[
              {name: 'решенные', value: data.personal.solvedAndAttempted.solvedCount, color: 'blue.6'},
              {name: 'начатые', value: data.personal.solvedAndAttempted.attemptedCount, color: 'gray.6'}
            ]}
            withTooltip
            withLabels
            tooltipDataSource={'segment'}
            mx="auto"
          />
        </Paper>

        <Paper mt={24} shadow="xs" p="xl">
          <Title size={'h3'}>Количество решенных задач по языкам</Title>
          <RadarChart
            mt={24}
            h={400}
            data={
              Object.entries(data.personal.languageCounts.reduce((prev, {language, count}) => {
                  prev[language] = count
                  return prev
                }, {Cpp: 0, Javascript: 0, Java: 0})
              ).map(([language, count]) => ({
                language,
                count
              }))
            }
            withPolarRadiusAxis
            mx="auto" series={[{name: 'count', color: 'blue'}]} dataKey={"language"}
          />
        </Paper>

        <Paper mt={24} shadow="xs" p="xl">
          <Title size={'h3'}>Количество решенных задач по сложности</Title>
          <RadarChart
            mt={24}
            h={400}
            data={
              Object.entries(data.personal.difficultyCounts).map(([difficulty, count]) => ({
                difficulty, count
              }))
            }
            withPolarRadiusAxis
            mx="auto" series={[{name: 'count', color: 'blue'}]} dataKey={"difficulty"}
          />
        </Paper>


        <Title size={'h1'} mt={42}>
          Статистика по платформе
        </Title>
        <Paper mt={24} shadow="xs" p="xl">
          <Title size={'h3'}>Топ задач по количеству решивших</Title>
          <Text></Text>
          <BarChart
            mt={24}
            h={300}
            data={data.general.topSolved}
            dataKey="problemSlug"
            tickLine="y"
            series={[{name: 'count', color: 'blue'}]}
          />
        </Paper>

        <Paper mt={24} shadow="xs" p="xl">
          <Title size={'h3'}>Топ задач по количеству приступивших к выполнению</Title>
          <Text></Text>
          <BarChart
            mt={24}
            h={300}
            data={data.general.topAttempted}
            dataKey="problemSlug"
            tickLine="y"
            series={[{name: 'count', color: 'blue'}]}
          />
        </Paper>

        <Paper mt={24} shadow="xs" p="xl">
          <Title size={'h3'}>Топ задач по количеству приступивших, но не решивших</Title>
          <Text></Text>
          <BarChart
            mt={24}
            h={300}
            data={data.general.topAttemptedNotSolved}
            dataKey="problemSlug"
            tickLine="y"
            series={[{name: 'count', color: 'blue'}]}
          />
        </Paper>

      </Container>
    </DotBackground>
  )
}
