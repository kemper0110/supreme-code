import {Editor} from "@monaco-editor/react";
import React, {useRef, useState} from "react";
import {Badge, Button, Flex, SegmentedControl, Stack, Text} from "@mantine/core";
import axios from "axios";
import {IconBrain, IconMoodCrazyHappy, IconPigMoney} from "@tabler/icons-react";
import {useMutation} from "@tanstack/react-query";
import {editor} from "monaco-editor";
import {LanguageValue} from "../types/LanguageValue.tsx";
import {SplitView} from "../components/SplitView.tsx";
import ICodeEditor = editor.ICodeEditor;

type RunRequest = {
  code: string
  language: string
}

const codeExamples = {
  javascript: `console.log("Hello, supremecode, from javascript")`,
  java: `class App {
  public static void main(String... args) {
    System.out.println("Hello, supremecode, from java");
  }
}
`,
  "c++": `#include <iostream>

int main() {
  std::cout << "Hello, supremecode, from c++";
}`
};

const languages = [
  {
    label: <Flex align={'center'} gap={4}><IconMoodCrazyHappy/><Text size={'lg'}>node.js 20</Text></Flex>,
    value: "javascript"
  },
  {
    label: <Flex align={'center'} gap={4}><IconBrain/><Text size={'lg'}>C++17 gcc:13.2.0</Text></Flex>,
    value: "c++"
  },
  {
    label: <Flex align={'center'} gap={4}><IconPigMoney/><Text size={'lg'}>java 21 corretto</Text></Flex>,
    value: "java"
  }
] as { label: string, value: LanguageValue }[]

export default function Playground() {
  const [language, setLanguage] = useState<LanguageValue>(languages[0].value)
  const editorRef = useRef<ICodeEditor>()

  const [result, setResult] = useState("")

  const runMutation = useMutation({
    mutationFn: (code: string) => axios.post('/api/', {code, language} as RunRequest),
    onSuccess: response => {
      setResult(response.data)
    }
  })

  const handleRun = () => {
    console.log(editorRef.current)
    const code = editorRef.current?.getValue()
    console.log({code})
    runMutation.mutate(code)
  }

  const cppCode = `
#include <iostream>
int main() {
  std::cout << "aboba";
}
`
  const keyboardHandler = (e: React.KeyboardEvent) => {
    if (e.key === 'F9') {
      handleRun()
    }
  }

  const onExample = () => {
    const code = codeExamples[language]
    editorRef.current?.setValue(code)
  };
  console.log({language})
  return (
    <SplitView left={leftRef => (
      <Stack ref={leftRef} className={''} onKeyDown={keyboardHandler}>
        <Flex justify={'space-between'} className={'shadow-md'} p={9}>
          <Flex columnGap={30} rowGap={8} align={'center'} wrap={'wrap'}>
            <SegmentedControl data={languages} value={language} onChange={value => setLanguage(value)}/>
            <Flex gap={30} align={'center'}>
              <Button onClick={handleRun}>
                Run
              </Button>
              <Button onClick={onExample}>
                Example code
              </Button>
            </Flex>
          </Flex>
        </Flex>
        <Editor onMount={editor => editorRef.current = editor}
                height="100%" language={language} defaultValue={cppCode}/>
      </Stack>
    )} right={
      <Stack className={'grow bg-slate-100'} gap={0}>
        <Flex className={'p-4 shadow-md'} align={'center'} gap={8}>
          Output: {
          runMutation.isPending ? (
            <Badge color={'blue'}>Running</Badge>
          ) : (
            <Badge color={'teal'}>Finished</Badge>
          )
        }
        </Flex>
        <pre className={'px-4 py-4 overflow-auto h-[100%]'}>
          {result}
        </pre>
      </Stack>
    }
    />
  )
}

