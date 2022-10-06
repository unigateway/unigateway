import React from 'react';
import clsx from 'clsx';
import {Theme, withStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import GatewayStatus from './GatewayStatus';
import OtherGateways from './OtherGateways';
import RecentLogs from './RecentLogs';

const useStyles = (theme: Theme) => ({
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  },
  paper: {
    display: 'flex',
    overflow: 'auto',
    flexDirection: 'column',
  },
  fixedHeight: {
    height: 240,
  },
});

class Dashboard extends React.Component<any> {

  render() {
    const { classes } = this.props
    const fixedHeightPaper = clsx(classes.paper, classes.fixedHeight);

    return (
      <Container maxWidth="lg" className={classes.container}>
        <Grid container spacing={3}>
          {/* Status */}
          <Grid item xs={12} md={8} lg={9}>
            <Paper className={fixedHeightPaper}>
              <GatewayStatus />
            </Paper>
          </Grid>
          {/* Other Gateways */}
          <Grid item xs={12} md={4} lg={3}>
            <Paper className={fixedHeightPaper}>
              <OtherGateways />
            </Paper>
          </Grid>
          {/* Recent Logs */}
          <Grid item xs={12}>
            <Paper className={classes.paper}>
              <RecentLogs />
            </Paper>
          </Grid>
        </Grid>
      </Container>
    );
  }
}

// @ts-ignore
export default withStyles(useStyles)(Dashboard)