import cx from 'clsx';
import { Box, Text, Group, rem } from '@mantine/core';
import { IconListSearch } from '@tabler/icons-react';
import classes from './index.module.css';

type Link = {
  label: string;
  link: string;
  order: number;
}


export default function TableOfContents({active, links}: {
  active: string;
  links: Link[];
}) {
  const items = links.map((item) => (
    <Box<'a'>
      component="a"
      href={item.link}
      onClick={(event) => event.preventDefault()}
      key={item.label}
      className={cx(classes.link, { [classes.linkActive]: active === item.link })}
      style={{ paddingLeft: `calc(${item.order} * var(--mantine-spacing-md))` }}
    >
      {item.label}
    </Box>
  ));

  return (
    <div>
      <Group mb="md">
        <IconListSearch style={{ width: rem(18), height: rem(18) }} stroke={1.5} />
        <Text>Содержание</Text>
      </Group>
      {items}
    </div>
  );
}
