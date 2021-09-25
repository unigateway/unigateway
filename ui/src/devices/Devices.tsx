import React, {useContext, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import {Box, Fab, Grid, InputAdornment, TextField} from "@material-ui/core";
import SearchIcon from '@material-ui/icons/Search';
import DeviceCard from "./DeviceCard";
import AddIcon from '@material-ui/icons/Add';
import FlipIcon from '@material-ui/icons/Flip';
import DeviceDetailsDialog, {DeviceForm} from "./DeviceDetailsDialog";
import {GatewayConfigurationContext} from "../App";
import {Device, GatewayConfiguration} from "../MqGatewayMutableTypes";

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      display: 'flex',
      flexWrap: 'wrap',
    },
    textField: {
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(1),
      width: '25ch',
    },
    container: {
      paddingTop: theme.spacing(4),
      paddingBottom: theme.spacing(4),
    },
    searchBox: {
      backgroundColor: 'white',
      padding: '15px',
      margin: 0
    },
    fabs: {
      position: 'absolute',
      bottom: theme.spacing(5),
      right: theme.spacing(8),
    },
    leftFab: {
      right: theme.spacing(1),
    },
  }),
);

export default function Devices() {
  const classes = useStyles();
  const { gatewayConfiguration, setGatewayConfiguration } = useContext(GatewayConfigurationContext)
  const devices = gatewayConfiguration.allDevices()
  const [filterPhrase, setFilterPhrase] = useState("")
  const [deviceCardSide, setDeviceCardSide] = React.useState<"config" | "action">("action");
  const [deviceDetailsDialogOpen, setDeviceDetailsDialogOpen] = React.useState(false)
  const [deviceDetailsToEdit, setDeviceDetailsToEdit] = React.useState<Device | null>(null)

  const toggleActionConfigSide = () => {
    if (deviceCardSide === "config") {
      setDeviceCardSide("action")
    } else {
      setDeviceCardSide("config")
    }
  }

  const openDeviceDetailsDialogEdit = (device : Device) => {
    setDeviceDetailsToEdit(device)
    setDeviceDetailsDialogOpen(true)
  }

  const openDeviceDetailsDialog = () => {
    setDeviceDetailsToEdit(null)
    setDeviceDetailsDialogOpen(true)
  }
  const closeDeviceDetailsDialog = () => {
    setDeviceDetailsToEdit(null)
    setDeviceDetailsDialogOpen(false)
  }

  const saveAndCloseDeviceDetailsDialog = (deviceForm: DeviceForm) => {
    setGatewayConfiguration(oldGatewayConfiguration => {
      const existingDevice = oldGatewayConfiguration.findDeviceByUuid(deviceForm.uuid!);
      if (existingDevice) {
        const {room, point} = oldGatewayConfiguration.deviceLocation(existingDevice.uuid)!
        if (room.name !== deviceForm.roomName || point.name !== deviceForm.pointName) {
          oldGatewayConfiguration.moveDeviceToPoint(existingDevice.uuid, deviceForm.roomName, deviceForm.pointName)
        }
        oldGatewayConfiguration.replaceDevice(DeviceForm.toDevice(deviceForm))
      } else {
        oldGatewayConfiguration.getRoom(deviceForm.roomName).getPoint(deviceForm.pointName).addDevice(DeviceForm.toDevice(deviceForm))
      }
      const newGatewayConfiguration = new GatewayConfiguration(oldGatewayConfiguration.configVersion, oldGatewayConfiguration.name, oldGatewayConfiguration.mqttHostname, oldGatewayConfiguration.rooms);
      if (oldGatewayConfiguration.hasAnyChanges()) {
        newGatewayConfiguration.isModified = true
      }
      return newGatewayConfiguration
    })
    setDeviceDetailsDialogOpen(false)
    setDeviceDetailsToEdit(null)
  }

  const deleteDevice = (device: Device) => {
    setGatewayConfiguration(oldGatewayConfiguration => {
      oldGatewayConfiguration.deleteDevice(device)
      const newGatewayConfiguration = new GatewayConfiguration(oldGatewayConfiguration.configVersion, oldGatewayConfiguration.name, oldGatewayConfiguration.mqttHostname, oldGatewayConfiguration.rooms);
      if (oldGatewayConfiguration.hasAnyChanges()) {
        newGatewayConfiguration.isModified = true
      }
      return newGatewayConfiguration
    })
    setDeviceDetailsDialogOpen(false)
    setDeviceDetailsToEdit(null)
  }

  return (
    <Box>
      <Box className={classes.searchBox}>
        <TextField
          id="search-devices-input"
          value={filterPhrase}
          onChange={(event: any) => setFilterPhrase(event.target.value)}
          placeholder="Filter"
          fullWidth
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
          }}
        />
      </Box>
      <Container maxWidth="xl" className={classes.container}>
        <Grid container spacing={3}>
          {devices.filter(device => device.name.toLowerCase().includes(filterPhrase.toLowerCase())).map((device: Device) => {
            return (<Grid item key={device.id} xs={12} md={6} lg={4} xl={3}><DeviceCard device={device} side={deviceCardSide} openDeviceDetailsEdit={() => openDeviceDetailsDialogEdit(device)} /></Grid>)
          })}

        </Grid>

        <div className={classes.fabs}>
          <Fab aria-label="Flip" className={classes.leftFab} color="secondary" size="large" onClick={toggleActionConfigSide}>
            <FlipIcon />
          </Fab>
          <Fab aria-label="Add" color="primary" size="large" onClick={openDeviceDetailsDialog}>
            <AddIcon />
          </Fab>
        </div>

      </Container>
      <DeviceDetailsDialog open={deviceDetailsDialogOpen} onClose={closeDeviceDetailsDialog} onSaveAndClose={saveAndCloseDeviceDetailsDialog} onDeleteDevice={deleteDevice} device={deviceDetailsToEdit} />
    </Box>
  );
}