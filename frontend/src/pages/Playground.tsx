import {Editor} from "@monaco-editor/react";
import {useRef, useState} from "react";
import {Button, Flex, SegmentedControl, Stack} from "@mantine/core";

export default function Playground() {
  const [language, setLanguage] = useState('javascript')
  const editorRef = useRef()

  const [result, setResult] = useState("")

  const handleRun = () => {
    const code = editorRef.current.getValue()
    console.log({code})


  }


  console.log({language})
  return (
    <Flex>
      <Stack className={'w-[70vw]'}>
        <Flex justify={'space-between'}>
          <SegmentedControl data={['javascript', 'c++', 'java']} value={language} onChange={v => setLanguage(v)}/>
          <Button onClick={handleRun}>
            Run
          </Button>
        </Flex>
        <Editor onMount={editor => editorRef.current = editor}
                height="100dvh" language={language} defaultValue="// some comment"/>
      </Stack>
      <div className={'w-[30vw]'}>
        {result}
      </div>
    </Flex>
  )
}
