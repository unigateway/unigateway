import React, {useContext} from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import RoomConfig from "../components/RoomConfig";
import {GatewayConfigurationContext} from "../App";

const useStyles = makeStyles((theme) => ({
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  }
}));

export default function Topology() {
  const classes = useStyles();
  const { gatewayConfiguration } = useContext(GatewayConfigurationContext);


  return (
        <Container maxWidth="xl" className={classes.container}>
          { gatewayConfiguration.rooms.map((room) => { return (<RoomConfig room={room} />); }) }
        </Container>
  );
}