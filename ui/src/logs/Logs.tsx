import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import LogViewer from "./LogViewer";
import {Paper} from "@material-ui/core";

const useStyles = makeStyles((theme) => ({
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4)
  },
  paper: {
    padding: theme.spacing(2),
    paddingTop: theme.spacing(3),
    paddingBottom: theme.spacing(3),
    maxHeight: "80vh",
    overflow: "auto"
  }
}));

export default function Logs() {
  const classes = useStyles();

  return (
    <Container maxWidth="xl" className={classes.container}>
      <Paper elevation={3} className={classes.paper}>
        <LogViewer readLogs={true} readInitialLogsOnStart={true} />
      </Paper>
    </Container>
  );
}