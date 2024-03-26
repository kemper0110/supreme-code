import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";

export function Description(props: { description: string }) {
  return <Markdown remarkPlugins={[remarkGfm]}
                   className={"p-4 pb-16 prose max-w-full overflow-y-auto max-h-screen"}>
    {props.description}
  </Markdown>;
}
