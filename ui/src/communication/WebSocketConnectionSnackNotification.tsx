import Snackbar from "@material-ui/core/Snackbar";
import React, {useCallback, useEffect, useState} from "react";
import MuiAlert, {AlertProps} from "@material-ui/lab/Alert";
import {ConnectionState} from "./GatewayWS";

interface WebSocketConnectionSnackNotificationProps {
  connectionState: ConnectionState
}


function Alert(props: AlertProps) {
  return <MuiAlert elevation={6} variant="filled" {...props} />;
}

export default function WebSocketConnectionSnackNotification(props: WebSocketConnectionSnackNotificationProps) {
  const {connectionState} = props

  const [open, setOpen] = useState(false)
  const [autoHideDuration, setAutoHideDuration] = useState<number | null>(null)
  const [message, setMessage] = useState("")

  const isSuccess = useCallback(() => {
    return connectionState === ConnectionState.CONNECTED
  }, [connectionState])

  useEffect(() => {
    if (isSuccess()) {
      setMessage("Connected to MqGateway successfully")
      setAutoHideDuration(3000)
      setOpen(true)
    } else {
      setMessage("Disconnected from MqGateway. Trying to reconnect...")
      setAutoHideDuration(null)
      setOpen(true)
    }
  }, [isSuccess])



  const handleClose = (event?: React.SyntheticEvent, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpen(false)
  };

  return (
    <Snackbar anchorOrigin={{vertical: "bottom", horizontal: "left"}} open={open} onClose={handleClose} autoHideDuration={autoHideDuration}>
      <Alert onClose={handleClose} severity={isSuccess() ? "success" : "warning" }>
        {message}
      </Alert>
    </Snackbar>
  )
}