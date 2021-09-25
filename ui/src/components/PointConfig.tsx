import React from "react";
import {Accordion, AccordionDetails, AccordionSummary, MenuItem, Select, TextField} from "@material-ui/core";
import {Point} from "../MqGatewayMutableTypes";
import DeviceConfig from "./DeviceConfig";

interface PointConfigProps {
  point: Point
}

export default function PointConfig(props: PointConfigProps) {

    const {name, portNumber, devices} = props.point

    const portsItems = []
    for (let i = 1; i <= 32; i++) {
      portsItems.push(<MenuItem value={i}>{i}</MenuItem>)
    }


    const devicesComponents = devices.map(device => {
      return (<DeviceConfig device={device} />)
    })

    return (
      <Accordion>
        <AccordionSummary>{name}</AccordionSummary>
        <AccordionDetails>
          <form noValidate autoComplete="off">
            <TextField id="name" label="Name" value={name} fullWidth />
            <Select id="port" value={portNumber} fullWidth>{portsItems}</Select>
            {devicesComponents}
          </form>
        </AccordionDetails>
      </Accordion>
    );
}