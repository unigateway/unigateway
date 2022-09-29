import Dialog from "@material-ui/core/Dialog";
import Button from "@material-ui/core/Button";
import {Backdrop, CircularProgress, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@material-ui/core";
import React, {useContext, useState} from "react";
import {makeStyles} from "@material-ui/core/styles";
import {GatewayCommunicationContext, GatewayConfigurationContext} from "../App";
import ConfigurationChangeSnackNotification from "./ConfigurationChangeSnackNotification";
import ConfigurationChangeErrorDialog from "./ConfigurationChangeErrorDialog";


const useStyles = makeStyles((theme) => ({
  backdrop: {
    zIndex: theme.zIndex.drawer + 1,
    color: '#fff',
  }
}))

interface ApplyConfigurationDialogProps {
  open: boolean
  onClose: () => void
}

export default function ApplyConfigurationDialog(props: ApplyConfigurationDialogProps) {

  const classes = useStyles();
  const { gatewayConnection } = useContext(GatewayCommunicationContext)
  const { setGatewayConfiguration, yamlConfiguration, setYamlConfiguration } = useContext(GatewayConfigurationContext)

  const {open, onClose} = props

  const [reloadingGateway, setReloadingGateway] = useState(false);
  const [configurationChangeSnackNotificationOpen, setConfigurationChangeSnackNotificationOpen] = useState(false);
  const [configurationChangeErrorDialogOpen, setConfigurationChangeErrorDialogOpen] = useState(false);
  const [configurationChangeResult, setConfigurationChangeResult] = useState({validationFailures: []});

  const reloadConfiguration = async (initialWaitMs: number = 0) => {
    setReloadingGateway(true)
    await new Promise(resolve => setTimeout(resolve, initialWaitMs));
    const {yaml, gatewayConfiguration} = await gatewayConnection.reconnect()
    setYamlConfiguration(yaml);
    setGatewayConfiguration(gatewayConfiguration)
    setReloadingGateway(false)
  }

  const handleAcceptApplyConfigurationDialog = async () => {
    try {
      await gatewayConnection.sendNewConfig(yamlConfiguration)
      onClose()
      setConfigurationChangeSnackNotificationOpen(true)
    } catch (e) {
      setConfigurationChangeErrorDialogOpen(true)
      setConfigurationChangeResult(e.response.data)
    }
    await reloadConfiguration(3000)
  }

  const handleRevertApplyConfigurationDialog = async () => {
    onClose()
    await reloadConfiguration()
  }

  const handleCloseApplyConfigurationDialog = () => {
    onClose()
  };

  const handleCloseConfigurationChangeSnackNotification = () => {
    setConfigurationChangeSnackNotificationOpen(false);
  };

  return (
    <>
      <Dialog
        open={open}
        onClose={handleCloseApplyConfigurationDialog}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">Apply configuration change?</DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            MqGateway application need to restart to apply new configuration. It will take a few minutes. You will see notification when new configuration is applied.
            <br />
            Do you want to continue?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseApplyConfigurationDialog} color="inherit">
            Cancel
          </Button>
          <Button onClick={handleRevertApplyConfigurationDialog} color="secondary">
            Revert
          </Button>
          <Button onClick={handleAcceptApplyConfigurationDialog} color="primary" autoFocus>
            Apply
          </Button>
        </DialogActions>
      </Dialog>
      <ConfigurationChangeSnackNotification open={configurationChangeSnackNotificationOpen} onClose={handleCloseConfigurationChangeSnackNotification} />
      <ConfigurationChangeErrorDialog open={configurationChangeErrorDialogOpen} onClose={() => setConfigurationChangeErrorDialogOpen(false)} saveResult={configurationChangeResult} />
      <Backdrop className={classes.backdrop} open={reloadingGateway}>
        <CircularProgress color="inherit" />
      </Backdrop>
    </>
  )
}