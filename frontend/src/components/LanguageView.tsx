import {IconBrain, IconMoodCrazyHappy, IconPigMoney} from "@tabler/icons-react";
import {Flex, Text} from "@mantine/core";

export const NodeView = () => {
  return (
    <Flex align={'center'} gap={4}><IconMoodCrazyHappy/><Text size={'lg'}>node.js 20</Text></Flex>
  )
}

export const CppView = () => {
  return (
    <Flex align={'center'} gap={4}><IconBrain/><Text size={'lg'}>C++17 gcc:13.2.0</Text></Flex>
  )
}

export const JavaView = () => {
  return (
    <Flex align={'center'} gap={4}><IconPigMoney/><Text size={'lg'}>java 21 corretto</Text></Flex>
  )
}
