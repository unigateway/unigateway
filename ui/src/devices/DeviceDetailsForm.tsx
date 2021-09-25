import React, {useContext, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {Box, Chip, FormControl, Input, InputLabel, MenuItem, Select, TextField, Typography} from "@material-ui/core";
import {DeviceType, WireColor} from "../communication/MqgatewayTypes";
import DeviceDetailsExtraConfig from "./extraConfig/DeviceDetailsExtraConfig";
import {GatewayConfigurationContext} from "../App";
import DeviceDetailsInternalDevices from "./internalDevices/DeviceDetailsInternalDevices";
import {DeviceForm} from "./DeviceDetailsDialog";

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
      width: 250,
    },
  },
};

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      textAlign: "left",
      '& .MuiTextField-root': {
        margin: theme.spacing(1),
      },
    },
    formControl: {
      margin: theme.spacing(1),
      minWidth: 240,
    },
    chips: {
      display: 'flex',
      flexWrap: 'wrap',
    },
    chip: {
      margin: 2,
    },
    extraConfigLabel: {
      margin: theme.spacing(1),
      marginTop: theme.spacing(3),
      color: "black",
    }
  })
);

interface DeviceDetailsFormProps {
  deviceForm: DeviceForm
  onPropertyChange: (deviceForm: DeviceForm) => void
  isInternalDevice?: boolean
  internalDeviceType?: DeviceType
}

export default function DeviceDetailsForm(props: DeviceDetailsFormProps) {

  props = { isInternalDevice: false, ...props }

  const classes = useStyles();

  const { gatewayConfiguration } = useContext(GatewayConfigurationContext)

  const { deviceForm, onPropertyChange, isInternalDevice, internalDeviceType } = props
  const room = gatewayConfiguration.getRoom(deviceForm.roomName)
  const allRooms = gatewayConfiguration.rooms.map(it => it.name)
  const allPointsForRoom = (roomName: string) => gatewayConfiguration.getRoom(roomName).points.map(it => it.name)

  const [points, setPoints] = useState<string[]>(room?.points.map(it => it.name) || [])


  const handleRoomChange = (event: React.ChangeEvent<{ value: unknown }>) => {
    const newRoomName = event.target.value as string
    onPropertyChange({...deviceForm, roomName: event.target.value as string})
    setPoints(allPointsForRoom(newRoomName))
  }
  const handlePointChange = (event: React.ChangeEvent<{ value: unknown }>) => {
    onPropertyChange({...deviceForm, pointName: event.target.value as string})
  }

  const handleNameChange = (event: React.ChangeEvent<{ value: string }>) => {
    onPropertyChange({...deviceForm, name: event.target.value})
  }

  const handleIdChange = (event: React.ChangeEvent<{ value: string }>) => {
    onPropertyChange({...deviceForm, id: event.target.value})
  }

  const handleTypeChange = (event: React.ChangeEvent<{ value: unknown }>) => {
    const type = event.target.value
    let wires = deviceForm.wires
    if (type === DeviceType[DeviceType.REFERENCE]) {
      wires = []
    }
    onPropertyChange({...deviceForm, type: event.target.value as DeviceType, config: new Map(), wires: wires})
  }

  const handleWiresChange = (event: React.ChangeEvent<{ value: unknown }>) => {
    onPropertyChange({...deviceForm, wires: event.target.value as WireColor[]})
  }

  const handleReferencedDeviceIdChange = (event: React.ChangeEvent<{ value: unknown }>) => {
    onPropertyChange({...deviceForm, referencedDeviceId: event.target.value as string})
  }

  const handleExtraConfigChange = (newConfig: any) => {
    onPropertyChange({...deviceForm, config: newConfig})
  }

  const handleInternalDevicesChange = (newInternalDevices: Map<string, DeviceForm>) => {
    onPropertyChange({...deviceForm, internalDevices: newInternalDevices})
  }

  const deviceTypesSelect = () => {
    if (!isInternalDevice) {
      return Object.keys(DeviceType)
        .filter(value => isNaN(Number(value)))
        .filter(deviceType => ![DeviceType[DeviceType.REFERENCE], DeviceType[DeviceType.MQGATEWAY]].includes(deviceType))
        .map(deviceType => {
          return (<MenuItem value={deviceType}>{deviceType}</MenuItem>)
        })
    } else {
      return [DeviceType[DeviceType.REFERENCE], DeviceType[internalDeviceType!]]
        .map(deviceType => {
          if (isNaN(Number(deviceType))) {
            return deviceType
          } else {
            // @ts-ignore
            return DeviceType[deviceType]
          }
        })
        .map( deviceType => (<MenuItem value={deviceType}>{deviceType}</MenuItem>))
    }
  }

  const roomsSelect = allRooms.map(roomName => {
      return (<MenuItem value={roomName}>{roomName}</MenuItem>)
    })

  const pointsSelect = points.map(point => {
      return (<MenuItem value={point}>{point}</MenuItem>)
    })

  const allDevicesIdsSelect = gatewayConfiguration.allDevices()
    .filter(device => device.type.toString() === DeviceType[internalDeviceType!])
    .map(device => {
      return (<MenuItem value={device.id}>{device.id}</MenuItem>)
    })

  const shouldShowExtraConfigForDevice = (type: DeviceType) => {
    switch (type.toString()) {
      case DeviceType[DeviceType.RELAY]: return true;
      case DeviceType[DeviceType.SWITCH_BUTTON]: return true;
      case DeviceType[DeviceType.MOTION_DETECTOR]: return true;
      case DeviceType[DeviceType.REED_SWITCH]: return true;
      case DeviceType[DeviceType.SHUTTER]: return true;
      case DeviceType[DeviceType.GATE]: return true;
      case DeviceType[DeviceType.BME280]: return true;
      case DeviceType[DeviceType.DHT22]: return true;
      default: return false;
    }
  }

  const shouldShowInternalDevicesForDevice = (type: DeviceType) => {
    if (isInternalDevice) {
      return false;
    }
    switch (type.toString()) {
      case DeviceType[DeviceType.SHUTTER]: return true;
      case DeviceType[DeviceType.GATE]: return true;
      default: return false;
    }
  }

  const allWiresSelect = Object.keys(WireColor)
    .filter(value => isNaN(Number(value)))
    .filter(value => !["ORANGE", "ORANGE_WHITE"].includes(value) )
    .map(wire => { return(<MenuItem key={wire} value={wire}>{wire}</MenuItem>) })

  return (
    <form className={classes.root} noValidate autoComplete="off">

      <Box display={isInternalDevice ? "none" : "block"}>
      <FormControl variant="outlined" className={classes.formControl} disabled={isInternalDevice} hidden={!isInternalDevice}>
        <InputLabel id="room-label">Room</InputLabel>
        <Select
          labelId="room-label"
          id="room"
          value={deviceForm.roomName}
          onChange={handleRoomChange}
          label="Room"
        >
          {roomsSelect}
        </Select>
      </FormControl>

      <FormControl variant="outlined" className={classes.formControl} hidden={isInternalDevice} disabled={isInternalDevice}>
        <InputLabel id="point-label">Point</InputLabel>
        <Select
          labelId="point-label"
          id="point"
          value={deviceForm.pointName}
          onChange={handlePointChange}
          label="Point"
        >
          {pointsSelect}
        </Select>
      </FormControl>
      </Box>


      <TextField id="name" label="Name" value={deviceForm.name} onChange={handleNameChange} variant="outlined" fullWidth />
      <TextField id="id" label="Id" value={deviceForm.id} onChange={handleIdChange} variant="outlined" fullWidth />
      <FormControl variant="outlined" className={classes.formControl}>
        <InputLabel id="type-label">Type</InputLabel>
        <Select
          labelId="type-label"
          id="type"
          value={deviceForm.type}
          onChange={handleTypeChange}
          label="Type"
        >
          {deviceTypesSelect()}
        </Select>
      </FormControl>

      <Box display={deviceForm.type.toString() === DeviceType[DeviceType.REFERENCE] ? "none" : "block"}>
        <FormControl variant="outlined" className={classes.formControl} fullWidth>
          <InputLabel id="wires-label">Wires</InputLabel>
          <Select labelId="wires-label" id="wires" multiple value={deviceForm.wires} onChange={handleWiresChange} input={<Input id="wires" />}
                  MenuProps={MenuProps}
                  renderValue={(selected) => (
                    <div className={classes.chips}>
                      {deviceForm.wires.map((wire) =>
                        (<Chip key={wire} label={wire} className={classes.chip} />)
                      )}
                    </div>
                  )}
          >
            {allWiresSelect}
          </Select>
        </FormControl>
      </Box>

      <Box display={deviceForm.type.toString() === DeviceType[DeviceType.REFERENCE] ? "block" : "none"}>
        <FormControl variant="outlined" className={classes.formControl} fullWidth>
          <InputLabel id="referenced-device-id-label">Referenced device ID</InputLabel>
          <Select
            labelId="referenced-device-id-label"
            id="referencedDeviceId"
            value={deviceForm.referencedDeviceId}
            onChange={handleReferencedDeviceIdChange}
            label="Referenced device ID"
            fullWidth
          >
            {allDevicesIdsSelect}
          </Select>
        </FormControl>
      </Box>

      <Box display={shouldShowExtraConfigForDevice(deviceForm.type) ? "block" : "none"}>
        <Typography className={classes.extraConfigLabel} variant="h6">
          Extra config
        </Typography>
        <FormControl className={classes.formControl} fullWidth>
          <DeviceDetailsExtraConfig deviceForm={deviceForm} onConfigChange={handleExtraConfigChange} />
        </FormControl>
      </Box>

      <Box display={shouldShowInternalDevicesForDevice(deviceForm.type) ? "block" : "none"}>
        <Typography className={classes.extraConfigLabel} variant="h6">
          Internal devices
        </Typography>
        <FormControl className={classes.formControl} fullWidth>
          <DeviceDetailsInternalDevices deviceForm={deviceForm} onChange={handleInternalDevicesChange} />
        </FormControl>
      </Box>
    </form>
  )

}
