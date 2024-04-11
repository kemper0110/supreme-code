import {Progress} from "@mantine/core";
import {useWindowScroll} from "@mantine/hooks";

export const ProgressBar = () => {
  const [scroll] = useWindowScroll();

  const scrollMaxValue = (() => {
    const body = document.body;
    const html = document.documentElement;
    const documentHeight = Math.max(
      body.scrollHeight,
      body.offsetHeight,
      html.clientHeight,
      html.scrollHeight,
      html.offsetHeight
    );
    const windowHeight = window.innerHeight;
    return documentHeight - windowHeight
  })()

  const progress = Math.floor(scroll.y / scrollMaxValue * 100);
  return (
    <Progress.Root color={'blue'} size={'xl'}>
      <Progress.Section value={progress} color={'blue'}>
        <Progress.Label>
          {progress}%
        </Progress.Label>
      </Progress.Section>
    </Progress.Root>
  );
};
