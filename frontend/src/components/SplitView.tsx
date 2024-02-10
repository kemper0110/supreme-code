import React, {ReactNode, useEffect, useRef, useState} from "react";
import {Flex} from "@mantine/core";
import {IconGripVertical} from "@tabler/icons-react";

export const SplitView = ({left, right}: { left: (ref: any) => ReactNode, right: ReactNode }) => {

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
      <div onMouseDown={onMouseDown}
           className={'flex items-center shrink-0 w-[20px] h-full bg-slate-300 cursor-col-resize'}>
        <IconGripVertical className={'text-slate-500'}/>
      </div>
      {right}
    </Flex>
  )
}
