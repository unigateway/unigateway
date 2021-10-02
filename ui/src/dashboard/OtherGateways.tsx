import React, {useContext, useEffect, useState} from 'react';
import Title from './Title';
import {Container, List, ListItem, ListItemText} from "@material-ui/core";
import {GatewayCommunicationContext} from "../App";
import OtherGateway from "../communication/OtherGateway";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles(() => ({
  listContainer: {
    padding: 0,
    overflow: "auto"
  }
}))


export default function OtherGateways() {

  const classes = useStyles();

  const { gatewayConnection } = useContext(GatewayCommunicationContext)
  const [data, setData] = useState<OtherGateway[]>([])

  useEffect(() => {
    gatewayConnection.fetchOtherGateways().then(result => {
      setData(result.filter(it => !it.ipAddress.startsWith("127")))
    })
    const interval = setInterval(() => {
      gatewayConnection.fetchOtherGateways().then(result => {
        setData(result.filter(it => !it.ipAddress.startsWith("127")))
      })
    }, 7000)
    return () => clearInterval(interval)

  }, [])

  const redirectToGateway = (gateway: OtherGateway) => {
    window.location.replace(`http://${gateway.ipAddress}:${gateway.portNumber}/ui`)
  }

  return (
    <React.Fragment>
      <Title>Other gateways</Title>
      <Container className={classes.listContainer}>
      <List dense={true} component="nav" disablePadding={true}>
        {data.map((gateway) =>
          (<ListItem button key={gateway.ipAddress} onClick={() => redirectToGateway(gateway)}>
            <ListItemText
              primary={gateway.name}
              secondary={gateway.ipAddress + ":" + gateway.portNumber}
            />
          </ListItem>)
        )}
      </List>
      </Container>
    </React.Fragment>
  );
}