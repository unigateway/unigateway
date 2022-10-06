import {makeStyles} from "@material-ui/core/styles";
import {Box, Card, CardContent, Typography} from "@material-ui/core";
import DeviceCardConfigSide from "./DeviceCardConfigSide";
import React from "react";
import DeviceCardActionSide from "./DeviceCardActionSide";
import {Device} from "../MqGatewayMutableTypes";
import clsx from "clsx";


const useStyles = makeStyles({
  root: {
    minWidth: 275,
  },
  additional: {
    fontSize: 13,
    height: '20px',
    overflow: "hidden",
    whiteSpace: "nowrap",
    color: "gray"
  },
  pos: {
    marginBottom: 12,
  },
  card: {
    height: '250px',
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between"
  },
  inactiveCard: {
    backgroundColor: "lightgray"
  },
  content: {
    flexGrow: 1,
    overflow: "auto",
    height: "110px"
  },
  deviceName: {
    height: "32px",
    textOverflow: "ellipsis",
    overflow: "hidden",
    whiteSpace: "nowrap"
  }
});

interface DeviceCardProps {
  device: Device
  side: "config" | "action"
}

export default function DeviceCard(props: DeviceCardProps) {
  const classes = useStyles();

  const device: Device = props.device;

  let cardContent;
  if (props.side === "config") {
    cardContent = (<DeviceCardConfigSide device={props.device} />)
  } else {
    cardContent = (<DeviceCardActionSide device={props.device} />)
  }

  return (
    <Card className={clsx(classes.card, {[classes.inactiveCard]: device.isModifiedOrNewDevice})}>
      <CardContent>
        <Typography variant="h5" component="h2" gutterBottom className={classes.deviceName}>
          {device.name}
        </Typography>
        <Box className={classes.content}>
          {cardContent}
        </Box>
      </CardContent>
    </Card>
  )
}
