import React from "react";
import {Accordion, AccordionDetails, AccordionSummary, Chip, Input, MenuItem, Select, TextField} from "@material-ui/core";
import {DeviceType, WireColor} from "../communication/MqgatewayTypes";
import {Device} from "../MqGatewayMutableTypes";


interface DeviceConfigProps {
  device: Device
}

export default function DeviceConfig(props: DeviceConfigProps) {


  const {name, id, type, wires } = props.device

  // @ts-ignore
  const deviceTypesSelect = Object.keys(DeviceType).filter(key => !isNaN(Number(DeviceType[key]))).map(deviceType => {
    return (<MenuItem value={deviceType}>{deviceType}</MenuItem>)
  })



  const allWires = Object.keys(WireColor).filter(key => {
    // @ts-ignore
    return !isNaN(Number(DeviceType[key]));
  })


  return (
    <Accordion>
      <AccordionSummary>{name}</AccordionSummary>
      <AccordionDetails>
        <form noValidate autoComplete="off">
          <TextField id="name" label="Name" value={name} fullWidth />
          <TextField id="id" label="Id" value={id} fullWidth />
          <Select id="type" value={type}>
            {deviceTypesSelect}
          </Select>
          <Select id="wires" multiple value={wires} input={<Input id="wires" />}
                  renderValue={(selected) => (
                    <div>
                      {wires.map((wire) =>
                        (<Chip key={wire} label={wire} />)
                      )}
                    </div>
                  )}
          >
            {allWires.map((wire) => (
              <MenuItem key={wire} value={wire}>
                {wire}
              </MenuItem>
            ))}
          </Select>
        </form>
      </AccordionDetails>
    </Accordion>
  );
}