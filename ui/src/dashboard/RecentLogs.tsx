import React from 'react';
import Link from '@material-ui/core/Link';
import {makeStyles, Theme} from '@material-ui/core/styles';
import Title from './Title';

const useStyles = makeStyles((theme: Theme) => ({
  seeMore: {
    marginTop: theme.spacing(3),
  },
}));

export default function RecentLogs() {
  const classes = useStyles();
  return (
    <React.Fragment>
      <Title>Recent logs</Title>
      <div className={classes.seeMore}>
        <Link color="primary" href="#" onClick={() => {}}>
          See more logs
        </Link>
      </div>
    </React.Fragment>
  );
}