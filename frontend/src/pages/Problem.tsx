import {Panel, PanelGroup, PanelResizeHandle} from "react-resizable-panels";
import React, {useRef} from "react";
import {editor} from "monaco-editor";
import {Editor} from "@monaco-editor/react";
import {Link, RichTextEditor} from '@mantine/tiptap';
import {useEditor} from '@tiptap/react';
import Highlight from '@tiptap/extension-highlight';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Superscript from '@tiptap/extension-superscript';
import SubScript from '@tiptap/extension-subscript';
import {IconGripHorizontal, IconGripVertical} from "@tabler/icons-react";
import {Button, Flex, Stepper, Title} from "@mantine/core";
import {useParams} from "react-router-dom";
import {useQuery} from "@tanstack/react-query";
import ICodeEditor = editor.ICodeEditor;

export default function Problem() {
  const {id} = useParams()
  const {data} = useQuery({queryKey: ['problem', id]})
  const {name, description, languages, difficulty} = data

  const {template, test, language} = languages[0]

  const step = 0

  const editorRef = useRef<ICodeEditor>()

  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      Link,
      Superscript,
      SubScript,
      Highlight,
      TextAlign.configure({ types: ['heading', 'paragraph'] }),
    ],
    content: description,
    editable: false,
  });

  return (
    <div className={'h-screen'}>
      <PanelGroup autoSaveId={'problem:[description-editor]'} direction={'horizontal'}>
        <Panel defaultSize={40}>
          <Title>
            {name}
          </Title>
          <RichTextEditor editor={editor}>
            <RichTextEditor.Content className={'overflow-y-scroll max-h-screen'}/>
          </RichTextEditor>
        </Panel>
        <PanelResizeHandle className={'bg-slate-200 flex items-center justify-center'}>
          <IconGripVertical className={'w-[15px] text-slate-500'}/>
        </PanelResizeHandle>
        <Panel>
          <PanelGroup autoSaveId={'problem:[code-test]'} direction={'vertical'}>
            <Panel className={'pb-[40px]'} defaultSize={80}>
              <Editor onMount={editor => editorRef.current = editor}
                      height="100%" language={language.toLowerCase()} defaultValue={template}/>
              <Flex justify={'end'} pr={20}>
                <Button>
                  Запустить
                </Button>
              </Flex>
            </Panel>
            <PanelResizeHandle className={'my-[10px] bg-slate-200 flex items-center justify-center'}>
              <IconGripHorizontal className={'h-[15px] text-slate-500'}/>
            </PanelResizeHandle>
            <Panel>
              <Test step={step}/>
            </Panel>
          </PanelGroup>
        </Panel>
      </PanelGroup>
    </div>
  )
}

const Test = ({step}) => {

  return (
    <div className={'p-4'}>
      <Stepper active={step}>
        <Stepper.Step label="Шаг 1" description="Отправить код на тесты">
          Шаг 1: Отправить код на тесты
        </Stepper.Step>
        <Stepper.Step label="Шаг 2" description="Сборка">
          Шаг 2: Сборка
        </Stepper.Step>
        <Stepper.Step label="Шаг 3" description="Тестирование">
          Шаг 3: Тестирование
        </Stepper.Step>
        <Stepper.Completed>
          Все тесты прошли успешно
        </Stepper.Completed>
      </Stepper>
    </div>
  )
}
