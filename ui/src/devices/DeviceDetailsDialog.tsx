import React, {useContext, useEffect, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import {TransitionProps} from '@material-ui/core/transitions';
import SaveIcon from '@material-ui/icons/Save';
import DeleteIcon from '@material-ui/icons/Delete';
import MoreIcon from '@material-ui/icons/MoreVert';
import {Container, Grid, ListItemIcon, ListItemText, Menu, MenuItem, Paper} from "@material-ui/core";
import DeviceDetailsForm from "./DeviceDetailsForm";
import {Device} from "../MqGatewayMutableTypes";
import {GatewayConfigurationContext} from "../App";
import {DeviceType, WireColor} from "../communication/MqgatewayTypes";
import DeviceDetailsYaml from "./DeviceDetailsYaml";

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    appBar: {
      position: 'relative',
    },
    title: {
      marginLeft: theme.spacing(2),
      flex: 1,
    },
    formContainer: {
      marginTop: 30
    },
    dialog: {
      backgroundColor: "black"
    },
    paper: {
      padding: theme.spacing(2),
      textAlign: 'left',
      color: theme.palette.text.secondary,
      paddingRight: theme.spacing(4)
    }
  }),
);

const Transition = React.forwardRef(function Transition(
  props: TransitionProps & { children?: React.ReactElement },
  ref: React.Ref<unknown>,
) {
  return <Slide direction="up" ref={ref} {...props} />;
});


export class DeviceForm {
  id: string
  name: string
  type: DeviceType
  wires: WireColor[]
  config: Map<string, string>
  internalDevices: Map<string, DeviceForm>
  referencedDeviceId: string | null
  roomName: string
  pointName: string


  constructor(id?: string, name?: string, type?: DeviceType, wires?: WireColor[], config?: Map<string, string>,
              internalDevices?: Map<string, DeviceForm>, referencedDeviceId?: string | null, roomName?: string, pointName?: string,
              readonly uuid?: string) {

    this.id = id || "";
    this.name = name || "";
    this.type = type || DeviceType.RELAY;
    this.wires = wires || [];
    this.config = config || new Map();
    this.internalDevices = internalDevices || new Map();
    this.referencedDeviceId = referencedDeviceId || null;
    this.roomName = roomName || "";
    this.pointName = pointName || "";
  }

  static toDevice(deviceForm: DeviceForm): Device {
    const internalDevices: Map<string, Device> = new Map(
      Array.from(deviceForm.internalDevices, ([deviceName, internalDevice]) => {
        return [
          deviceName,
          DeviceForm.toDevice(internalDevice)
        ]
      })
    )
    return new Device(deviceForm.id, deviceForm.name, deviceForm.type, deviceForm.wires, deviceForm.config, internalDevices,
      deviceForm.referencedDeviceId, true, () => Promise.reject(), deviceForm.uuid)
  }

  static fromDevice(device: Device, roomName: string, pointName: string): DeviceForm {
    const internalDevicesForms: Map<string, DeviceForm> = new Map(
      Array.from(device.internalDevices, ([deviceName, internalDevice]) => {
        return [
          deviceName,
          new DeviceForm(internalDevice.id, internalDevice.name, internalDevice.type, internalDevice.wires, internalDevice.config, new Map(),
            internalDevice.referencedDeviceId, roomName, pointName, internalDevice.uuid)
        ]
      })
    )
    return new DeviceForm(device.id, device.name, device.type, device.wires, device.config, internalDevicesForms, device.referencedDeviceId,
      roomName, pointName, device.uuid)
  }
}

interface DeviceDetailsDialogProps {
  open: boolean,
  onClose: () => void,
  onSaveAndClose: (deviceForm: DeviceForm) => void,
  device: Device | null,
  onDeleteDevice: (device: Device) => void
}

export default function DeviceDetailsDialog(props: DeviceDetailsDialogProps) {
  const classes = useStyles();
  const {gatewayConfiguration} = useContext(GatewayConfigurationContext)

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deviceForm, setDeviceForm] = useState(new DeviceForm())

  const {device, open, onClose, onSaveAndClose, onDeleteDevice} = props

  useEffect(() => {
    if (device) {
      const { room, point } = gatewayConfiguration.deviceLocation(device.uuid)!
      if (room && point) {
        setDeviceForm(DeviceForm.fromDevice(device, room.name, point.name))
      } else {
        setDeviceForm(new DeviceForm())
      }
    } else {
      setDeviceForm(new DeviceForm())
    }
  }, [device])

  const handleOpenExtraMenu = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleExtraMenuClose = () => {
    setAnchorEl(null);
  };

  const handleClose = () => { setDeviceForm(new DeviceForm()); onClose() }
  const handleSaveAndClose = () => { onSaveAndClose(deviceForm) }
  const handleDeviceDeletion = () => {
    if (device) {
      onDeleteDevice(device)
      handleExtraMenuClose()
    }
  }

  const handleDeviceChange = (deviceForm: DeviceForm) => {
    setDeviceForm(deviceForm)
  }

  return (
    <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition} PaperProps={{style: {backgroundColor: '#fafafa'}}}>
      <AppBar className={classes.appBar}>
        <Toolbar>
          <IconButton edge="start" color="inherit" onClick={handleClose} aria-label="close">
            <CloseIcon />
          </IconButton>
          <Typography variant="h6" className={classes.title}>
            Device details
          </Typography>
          <Button
            variant="contained"
            color="secondary"
            size="large"
            startIcon={<SaveIcon />}
            onClick={handleSaveAndClose}
          >
            Save
          </Button>
          {device && <IconButton aria-label="display more actions" edge="end" color="inherit" aria-controls="simple-menu" aria-haspopup="true"
                                 onClick={handleOpenExtraMenu}>
            <MoreIcon />
          </IconButton>
          }
          <Menu
            id="simple-menu"
            anchorEl={anchorEl}
            keepMounted
            open={Boolean(anchorEl)}
            onClose={handleExtraMenuClose}
            getContentAnchorEl={null}
            anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
            transformOrigin={{ vertical: "top", horizontal: "left" }}
          >
            <MenuItem onClick={handleDeviceDeletion}>
              <ListItemIcon>
                <DeleteIcon />
              </ListItemIcon>
              <ListItemText primary="Delete" />
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Container className={classes.formContainer}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <Paper className={classes.paper}>
              <DeviceDetailsForm deviceForm={deviceForm} onPropertyChange={handleDeviceChange} />
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Paper className={classes.paper} >
              <DeviceDetailsYaml device={deviceForm || null} />
            </Paper>
          </Grid>
        </Grid>
      </Container>

    </Dialog>
  )

}