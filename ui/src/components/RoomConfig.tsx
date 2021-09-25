import React from "react";
import {Accordion, AccordionDetails, AccordionSummary, TextField} from "@material-ui/core";
import PointConfig from "./PointConfig";
import {Room} from "../MqGatewayMutableTypes";

interface RoomConfigProps {
  room: Room
}

export default function RoomConfig(props: RoomConfigProps) {

  const {name, points} = props.room;

  let pointsComponents = points.map(point => {
    return (<PointConfig point={point} />)
  })

  return (
    <Accordion>
      <AccordionSummary>{name}</AccordionSummary>
      <AccordionDetails>
        <form noValidate autoComplete="off">
          <TextField id="name" label="Name" value={name} fullWidth />
          {pointsComponents}
        </form>
      </AccordionDetails>
    </Accordion>
  );
}