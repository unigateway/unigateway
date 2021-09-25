import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';

const useStyles = makeStyles((theme) => ({
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  }
}));

export default function Logs() {
  const classes = useStyles();


  return (
        <Container maxWidth="xl" className={classes.container}>
          Logs
        </Container>
  );
}