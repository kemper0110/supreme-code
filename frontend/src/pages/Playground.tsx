import {Editor} from "@monaco-editor/react";
import React, {ReactNode, useEffect, useRef, useState} from "react";
import {Badge, Button, Flex, SegmentedControl, Stack} from "@mantine/core";
import axios from "axios";
import {IconGripVertical} from "@tabler/icons-react";
import {useMutation} from "@tanstack/react-query";

type RunRequest = {
  code: string
  language: string
}

export default function Playground() {
  const [language, setLanguage] = useState('javascript')
  const editorRef = useRef()

  const [result, setResult] = useState("")

  const runMutation = useMutation({
    mutationFn: (code: string) => axios.post('/api/', {code, language}),
    onSuccess: response => {
      setResult(response.data)
    }
  })

  const handleRun = () => {
    const code = editorRef.current.getValue()
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
    if(e.key === 'F9') {
      handleRun()
    }
  }

  console.log({language})
  const languages = ['javascript', 'c++', 'java']
  return (
    <SplitView left={leftRef => (
      <Stack ref={leftRef} className={''} onKeyDown={keyboardHandler}>
        <Flex justify={'space-between'} className={'shadow-md'} p={9}>

          <Flex gap={30} align={'center'}>
            <SegmentedControl data={languages} value={language} onChange={v => setLanguage(v)}/>
            <Button onClick={handleRun}>
              Run
            </Button>
          </Flex>
        </Flex>
        <Editor onMount={editor => editorRef.current = editor}
                height="100dvh" language={language} defaultValue={cppCode}/>
      </Stack>
    )} right={
      <div className={'grow bg-slate-100'}>
        <div className={'p-4 shadow-md'}>
          Output: {
          runMutation.isPending ? (
            <Badge color={'blue'}>Running</Badge>
          ) : (
            <Badge color={'teal'}>Finished</Badge>
          )
        }
        </div>
        <pre className={'px-4 py-2'}>
          {result}
        </pre>
      </div>
    }
    />
  )
}

const SplitView = ({left, right}: { left: (ref: any) => ReactNode, right: ReactNode }) => {

  const [leftWidth, setLeftWidth] = useState<number>(500)
  const leftRef = useRef<any>()

  useEffect(() => {
    if (!leftRef.current) return
    leftRef.current.style.width = leftWidth + 'px'
  }, [leftRef, leftWidth]);

  const [separatorXPosition, setSeparatorXPosition] = useState<undefined | number>(undefined);
  const [dragging, setDragging] = useState(false);

  const onMouseDown = (e: React.MouseEvent) => {
    setSeparatorXPosition(e.clientX);
    setDragging(true);
  };
  const onMouseMove = (e: React.MouseEvent) => {
    if (dragging && leftWidth && separatorXPosition) {
      const newLeftWidth = leftWidth + e.clientX - separatorXPosition;
      setSeparatorXPosition(e.clientX);

      if (newLeftWidth < 500) {
        setLeftWidth(500)
      } else {
        setLeftWidth(newLeftWidth);
      }
    }
  };

  const onMouseUp = () => {
    setDragging(false);
  };

  useEffect(() => {
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);

    return () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };
  });

  return (
    <Flex className={'h-screen'}>
      {left(leftRef)}
      <div onMouseDown={onMouseDown} className={'flex items-center shrink-0 w-[20px] h-full bg-slate-300 cursor-col-resize'}>
        <IconGripVertical className={'text-slate-500'}/>
      </div>
      {right}
    </Flex>
  )
}
