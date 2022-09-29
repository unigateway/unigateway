import React from "react";
import {Point} from "../MqGatewayMutableTypes";
import {ListItem, ListItemIcon, ListItemText} from "@material-ui/core";
import {Room as PointIcon} from "@material-ui/icons";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles((theme) => ({
  nested: {
    paddingLeft: theme.spacing(4),
  },
}));


interface PointComponentProps {
  point: Point,
  onClick: () => void
}

export default function PointComponent(props: PointComponentProps) {
  const classes = useStyles()
  const {name, portNumber, devices} = props.point


  return (
    <>
      <ListItem button className={classes.nested} onClick={props.onClick}>
        <ListItemIcon>
          <PointIcon />
        </ListItemIcon>
        <ListItemText primary={name} secondary={"Port: " + portNumber + " | No. devices: " + devices.length} />
      </ListItem>
    </>
  );
}