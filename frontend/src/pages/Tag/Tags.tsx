import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {Tag} from "./TagsLoader.ts";
import {Button, Container, Modal, Table, TextInput} from "@mantine/core";
import {IconPencil, IconX} from "@tabler/icons-react";
import {api} from "../../api/api.ts";
import {useDisclosure} from '@mantine/hooks';
import {useState} from "react";
import {DotBackground} from "../../components/Background.tsx";
import {tagsQueryKey, useTags} from "../shared/tags.ts";

export default function Tags() {
  const {data} = useTags()
  const queryClient = useQueryClient();
  const deleteTag = useMutation({
    mutationFn: async (id: number) => (await api.delete(`/api/tag/${id}`)).data,
    onSettled: () => {
      queryClient.invalidateQueries({queryKey: tagsQueryKey})
    }
  })
  const updateOrCreateTag = useMutation({
    mutationFn: async (data: { id?: number | undefined, name: string }) => (await api.post(`/api/tag`, data)).data,
    onSettled: () => {
      console.log('settled update')
      queryClient.invalidateQueries({queryKey: tagsQueryKey})
    }
  })

  const [createOpened, {open: openCreate, close: closeCreate}] = useDisclosure(false);
  const [updateOpened, {open: openUpdate, close: closeUpdate}] = useDisclosure(false);
  const [createTagName, setCreateTagName] = useState('')
  const [updateTagId, setUpdateTagId] = useState(0)
  const [updateTagName, setUpdateTagName] = useState('')

  return (
    <DotBackground className={'py-8 h-[calc(100dvh-99px)]'}>
      <Container size={'xl'} className={'h-full p-4 mx-auto bg-white shadow-lg'}>
        <Modal opened={createOpened} onClose={closeCreate} title="Создание тега">
          <TextInput value={createTagName} onChange={(e) => setCreateTagName(e.target.value)}/>
          <Button
            loading={updateOrCreateTag.isPending}
            style={{marginTop: 12}}
            onClick={async () => {
              await updateOrCreateTag.mutateAsync({name: createTagName})
              closeCreate()
            }}>
            Сохранить
          </Button>
        </Modal>
        <Modal opened={updateOpened} onClose={closeUpdate} title="Обновление тега">
          <TextInput value={updateTagName} onChange={(e) => setUpdateTagName(e.target.value)}/>
          <Button
            loading={updateOrCreateTag.isPending}
            className={'mt-3'}
            onClick={async () => {
              await updateOrCreateTag.mutateAsync({id: updateTagId, name: updateTagName})
              closeUpdate()
            }}>
            Сохранить
          </Button>
        </Modal>

        <Button variant="default" onClick={openCreate}>
          Создать тег
        </Button>
        <Table className={'mt-4'} data={{
          head: [
            "ID", "Название", ""
          ],
          body: data!.map(tag => [tag.id, tag.name, <div className={'flex gap-1'}>
            <Button onClick={() => {
              setUpdateTagId(tag.id)
              setUpdateTagName(tag.name)
              openUpdate()
            }}>
              <IconPencil/>
            </Button>
            <Button onClick={() => deleteTag.mutate(tag.id)}>
              <IconX/>
            </Button>
          </div>])
        }}/>
      </Container>
    </DotBackground>
  )
}
