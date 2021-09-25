import {Box, ListItemText, Menu, MenuItem} from "@material-ui/core";
import React from "react";
import {makeStyles} from "@material-ui/core/styles";
import Notification from "./Notification";

const useStyles = makeStyles(() => ({
  root:{
    width: 600,
    maxWidth: 600
  },
  listItem: {
    width: 500,
    whiteSpace: "normal"
  },
  menuItemIconBox: {
    paddingRight: 13
  }
}))

interface NotificationsMenuProps {
  anchor: null | HTMLElement
  notifications: Notification[]
  onClose: () => void
}

export default function NotificationsMenu(props: NotificationsMenuProps) {

  const classes = useStyles();

  const {anchor, onClose, notifications} = props

  return (
    <Menu
      id="simple-menu"
      anchorEl={anchor}
      keepMounted
      open={Boolean(anchor)}
      onClose={onClose}
      getContentAnchorEl={null}
      anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
      transformOrigin={{ vertical: "top", horizontal: "left" }}
      className={classes.root}
    >
      {notifications.map(notification =>
        <MenuItem key={notification.id} onClick={() => { onClose(); notification.action() }} className={classes.listItem}>
          <Box className={classes.menuItemIconBox}>{notification.icon}</Box>
          <ListItemText primary={notification.primaryText} secondary={notification.secondaryText} />
        </MenuItem>
      )}
    </Menu>
  )
}