import React from 'react';
import Link from '@material-ui/core/Link';
import {makeStyles, Theme} from '@material-ui/core/styles';
import Title from './Title';
import LogViewer from "../logs/LogViewer";
import {Container} from "@material-ui/core";
import {Link as RouterLink} from "react-router-dom";

const useStyles = makeStyles((theme: Theme) => ({
  seeMore: {
    marginTop: theme.spacing(3),
    paddingLeft: theme.spacing(2),
    paddingBottom: theme.spacing(2),
  },
  logsContainer: {
    maxHeight: "220px",
    overflow: "auto"
  }
}));

export default function RecentLogs() {
  const classes = useStyles();
  return (
    <React.Fragment>
      <Title>Recent logs</Title>
      <Container className={classes.logsContainer}>
        <LogViewer readLogs={true} readInitialLogsOnStart={true} maxElements={8} />
      </Container>
      <div className={classes.seeMore}>
        <Link component={RouterLink} color="primary" to="/logs">
          See more logs
        </Link>
      </div>
    </React.Fragment>
  );
}