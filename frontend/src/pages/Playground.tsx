import {Editor} from "@monaco-editor/react";
import {useRef, useState} from "react";
import {Button, Flex, SegmentedControl, Stack} from "@mantine/core";
import axios from "axios";

export default function Playground() {
  const [language, setLanguage] = useState('javascript')
  const editorRef = useRef()

  const [result, setResult] = useState("")

  const handleRun = () => {
    const code = editorRef.current.getValue()
    console.log({code})

    axios.post('/api/', code, {
      headers: {
        "Content-Type": "text/plain"
      }
    })
      .then(res => setResult(res.data))
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
                height="100dvh" language={language} defaultValue={`
#include <iostream>
int main() {
  std::cout << "aboba";
}
`}/>
      </Stack>
      <div className={'w-[30vw] p-5'}>
        {result}
      </div>
    </Flex>
  )
}
