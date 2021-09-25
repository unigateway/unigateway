import React, {useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {Box} from "@material-ui/core";
import {DeviceType} from "../../communication/MqgatewayTypes";
import InternalDevicePresentation from "./InternalDevicePresentation";
import InternalDeviceEditPopup from "./InternalDeviceEditPopup";
import {DeviceForm} from "../DeviceDetailsDialog";


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      display: 'flex',
      justifyContent: 'center',
      flexWrap: 'wrap',
      '& > *': {
        margin: theme.spacing(0.5),
      },
    }
  })
);

interface DeviceDetailsInternalDevicesProps {
  deviceForm: DeviceForm
  onChange: (internalDevices: Map<string, DeviceForm>) => void
}

export default function DeviceDetailsInternalDevices(props: DeviceDetailsInternalDevicesProps) {
  const classes = useStyles();

  const { deviceForm, onChange } = props
  const [editPopupOpen, setEditPopupOpen] = useState(false)
  const [editedDevice, setEditedDevice] = useState<DeviceForm | null>(null)
  const [editedDeviceName, setEditedDeviceName] = useState<string>("")
  const [editedDeviceType, setEditedDeviceType] = useState<DeviceType>(DeviceType.RELAY)

  const editInternalDevice = (name: string, type: DeviceType) => {
    setEditPopupOpen(true)
    setEditedDevice(deviceForm.internalDevices.get(name)!)
    setEditedDeviceName(name)
    setEditedDeviceType(type)
  }

  const clearInternalDevice = (name: string) => {
    const newInternalDevices = new Map<string, DeviceForm>(deviceForm.internalDevices)
    newInternalDevices.delete(name)
    onChange(newInternalDevices)
  }

  const handleEditPopupClose = () => {
    setEditPopupOpen(false)
    setEditedDevice(null)
  }

  const handleEditPopupSaveAndClose = (name: string, internalDeviceForm: DeviceForm) => {
    setEditPopupOpen(false)
    setEditedDevice(null)

    const newInternalDevices = new Map<string, DeviceForm>(deviceForm.internalDevices)
    newInternalDevices.set(name, internalDeviceForm)
    onChange(newInternalDevices)
  }

  const internalDevicesPresentations = (deviceForm: DeviceForm) => {
    switch (deviceForm.type.toString()) {
      case DeviceType[DeviceType.SHUTTER]: return (
          <Box className={classes.root}>
            <InternalDevicePresentation
              displayName="Stop relay"
              name="stopRelay"
              deviceType={DeviceType.RELAY}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "stopRelay")}
            />
            <InternalDevicePresentation
              displayName="Up/down relay"
              name="upDownRelay"
              deviceType={DeviceType.RELAY}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "upDownRelay")}
            />
          </Box>
        );
        case DeviceType[DeviceType.GATE]: return (
          <Box className={classes.root}>
            <InternalDevicePresentation
              displayName="Action button"
              name="actionButton"
              deviceType={DeviceType.EMULATED_SWITCH}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "actionButton")}
            />
            <InternalDevicePresentation
              displayName="Stop button"
              name="stopButton"
              deviceType={DeviceType.EMULATED_SWITCH}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "stopButton")}
            />
            <InternalDevicePresentation
              displayName="Open button"
              name="openButton"
              deviceType={DeviceType.EMULATED_SWITCH}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "openButton")}
            />
            <InternalDevicePresentation
              displayName="Close button"
              name="closeButton"
              deviceType={DeviceType.EMULATED_SWITCH}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "closeButton")}
            />
            <InternalDevicePresentation
              displayName="Open reed switch"
              name="openReedSwitch"
              deviceType={DeviceType.REED_SWITCH}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "openReedSwitch")}
            />
            <InternalDevicePresentation
              displayName="Closed reed switch"
              name="closedReedSwitch"
              deviceType={DeviceType.REED_SWITCH}
              onClick={editInternalDevice}
              onDelete={clearInternalDevice}
              isSet={isInternalDeviceSetUp(deviceForm, "closedReedSwitch")}
            />
          </Box>
        );
        default: return (<Box>Nothing</Box>)
      }
    }

  const isInternalDeviceSetUp = (deviceForm: DeviceForm, internalDeviceName: string) => {
    return deviceForm.internalDevices.has(internalDeviceName)
  }

  return (
    <Box>
    { internalDevicesPresentations(props.deviceForm) }
      <InternalDeviceEditPopup
        open={editPopupOpen}
        onClose={handleEditPopupClose}
        internalDeviceForm={editedDevice}
        name={editedDeviceName}
        deviceType={editedDeviceType}
        onSaveAndClose={handleEditPopupSaveAndClose}
      />
    </Box>
  )

}
