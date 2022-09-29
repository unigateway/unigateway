import Snackbar from "@material-ui/core/Snackbar";
import React from "react";
import MuiAlert, {AlertProps} from "@material-ui/lab/Alert";

interface ConfigurationChangeSnackNotificationProps {
  open: boolean
  onClose: () => void
}


function Alert(props: AlertProps) {
  return <MuiAlert elevation={6} variant="filled" {...props} />;
}

export default function ConfigurationChangeSnackNotification(props: ConfigurationChangeSnackNotificationProps) {
  const {open, onClose} = props

  const handleClose = (event?: React.SyntheticEvent, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    onClose();
  };

  return (
    <Snackbar open={open} onClose={onClose} autoHideDuration={6000}>
      <Alert onClose={handleClose} severity="success">
        Successfully saved new configuration for MqGateway. MqGateway will reload now. Please wait...
      </Alert>
    </Snackbar>
  )
}