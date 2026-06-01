import {Button, Container, Group, Text, Title} from "@mantine/core";
import {Link} from "react-router-dom";
import classes from "../Page500/index.module.css";

export default function Page403() {
  return (
    <div className={classes.root}>
      <Container>
        <div className={classes.label}>403</div>
        <Title className={classes.title}>Access denied</Title>
        <Text size="lg" ta="center" className={classes.description}>
          You do not have enough privileges to open this page.
        </Text>
        <Group justify="center">
          <Button component={Link} to="/" variant="white" size="md">
            Go to main page
          </Button>
        </Group>
      </Container>
    </div>
  );
}
