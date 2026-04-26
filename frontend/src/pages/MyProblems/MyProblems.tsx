import {Button, Container, Table} from "@mantine/core";
import {DotBackground} from "../../components/Background.tsx";
import {useNavigate} from "react-router-dom";
import {myProblemsQueryKey, useMyProblemsQuery} from "./MyProblemsLoader.ts";
import {IconPencil, IconX} from "@tabler/icons-react";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {api} from "../../api/api.ts";
import {usePlatformConfigQuery} from "../shared/PlatformConfig.ts";
import {useTags} from "../shared/tags.ts";

export default function MyProblems() {
  const navigate = useNavigate()
  const {data} = useMyProblemsQuery()
  const queryClient = useQueryClient()
  const {data: platformConfig} = usePlatformConfigQuery()
  const {data: tags} = useTags()
  const deleteProblem = useMutation({
    mutationFn: async (id: number) => (await api.delete(`/api/my-problem/${id}`)).data,
    onSettled: () => {
      queryClient.invalidateQueries({
        queryKey: myProblemsQueryKey
      })
    }
  })

  return <DotBackground className={'py-8 h-[calc(100dvh-99px)]'}>
    <Container size={'xl'} className={'h-full p-4 mx-auto bg-white shadow-lg'}>
      <Button variant="default" onClick={() => {
        navigate("/my-problem/create")
      }}>
        Создать задачу
      </Button>

      <Table className={'mt-4'} data={{
        head: ['ID', 'Название', 'Сложность', 'Теги', 'Языки', ''],
        body: data!.map(p => [
          p.id, p.name, p.difficulty,
          p.tags.map(t => tags!.find(tag => tag.id === t)?.name).join(', '),
          p.languages.map(id => platformConfig!.languages[id].name).join(', '),
          <div className={'flex gap-1'}>
            <Button onClick={() => {
              navigate(`/my-problem/update/${p.id}`)
            }}>
              <IconPencil/>
            </Button>
            <Button loading={deleteProblem.isPending} onClick={() => deleteProblem.mutate(p.id)}>
              <IconX/>
            </Button>
          </div>
        ]),
      }}/>

    </Container>
  </DotBackground>
}
