import React from 'react';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import DashboardIcon from '@material-ui/icons/Dashboard';
import SettingsInputComponentIcon from '@material-ui/icons/SettingsInputComponent';
import SwapHorizontalCircleIcon from '@material-ui/icons/SwapHorizontalCircle';
import AssignmentIcon from '@material-ui/icons/Assignment';
import {Link as RouterLink} from "react-router-dom";

export const mainListItems = (
  <div>
    <ListItem button component={RouterLink} to="/">
      <ListItemIcon>
        <DashboardIcon />
      </ListItemIcon>
      <ListItemText primary="Dashboard" />
    </ListItem>
    <ListItem button component={RouterLink} to="/devices">
      <ListItemIcon>
        <SettingsInputComponentIcon />
      </ListItemIcon>
      <ListItemText primary="Devices" />
    </ListItem>
    <ListItem button component={RouterLink} to="/rules" style={{display: "none"}}>
      <ListItemIcon>
        <SwapHorizontalCircleIcon />
      </ListItemIcon>
      <ListItemText primary="Rules" />
    </ListItem>
  </div>
);

export const secondaryListItems = (
  <div>
    <ListItem button component={RouterLink} to="/logs">
      <ListItemIcon>
        <AssignmentIcon />
      </ListItemIcon>
      <ListItemText primary="Logs" />
    </ListItem>
  </div>
);
