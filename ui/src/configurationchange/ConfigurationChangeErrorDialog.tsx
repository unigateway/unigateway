import {Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Typography} from "@material-ui/core";
import React from "react";

type ConfigurationChangeErrorDialogProps = {
  open: boolean
  onClose: () => void
  saveResult: any
}

export default function ConfigurationChangeErrorDialog(props: ConfigurationChangeErrorDialogProps) {

  const {open, saveResult, onClose} = props;

  const handleClose = () => {
    onClose();
  };

  return (
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">{"Could not save new MqGateway configuration"}</DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            {!saveResult.jsonValidationSucceeded && (<Typography>Json Schema validation failed - check structure of configuration</Typography>)}
            <ul>
            {saveResult.validationFailures && saveResult.validationFailures.map((failure: any) => (<li key={failure.description}>{failure.description}</li> ))}
            </ul>
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} color="primary" autoFocus>
            OK
          </Button>
        </DialogActions>
      </Dialog>
  )

}