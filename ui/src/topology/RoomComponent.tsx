import React from "react";
import {Point, Room} from "../MqGatewayMutableTypes";
import {List, ListItem, ListItemIcon, ListItemText} from "@material-ui/core";
import MeetingRoomIcon from "@material-ui/icons/MeetingRoom";
import {makeStyles} from "@material-ui/core/styles";
import PointComponent from "./PointComponent";
import {AddCircle as AddCircleIcon} from "@material-ui/icons";


const useStyles = makeStyles((theme) => ({
  nested: {
    paddingLeft: theme.spacing(4),
  },
  roomText: {
    fontWeight: "bold"
  },
  addPointText: {
    color: "gray"
  }
}));


interface RoomComponentProps {
  room: Room
  onClick: () => void
  onPointClick: (point: Point | null) => void
}

export default function RoomComponent(props: RoomComponentProps) {
  const classes = useStyles()
  const {name, points} = props.room


  return (
    <>
      <ListItem button onClick={props.onClick}>
        <ListItemIcon>
          <MeetingRoomIcon />
        </ListItemIcon>
        <ListItemText primary={name} classes={{ primary: classes.roomText }} />
      </ListItem>
      <List component="div" disablePadding>
        {points.map(point => <PointComponent key={point.uuid} point={point} onClick={() => props.onPointClick(point)} />)}
        <ListItem button className={classes.nested}>
          <ListItemIcon>
            <AddCircleIcon />
          </ListItemIcon>
          <ListItemText primary="Add point" classes={{ primary: classes.addPointText }} onClick={() => props.onPointClick(null)} />
        </ListItem>
      </List>
    </>
  );
}