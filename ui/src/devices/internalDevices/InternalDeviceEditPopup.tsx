import React, {useEffect, useState} from 'react';
import {Button, Dialog, DialogActions, DialogContent, DialogTitle} from "@material-ui/core";
import DeviceDetailsForm from "../DeviceDetailsForm";
import {DeviceType} from "../../communication/MqgatewayTypes";
import {DeviceForm} from "../DeviceDetailsDialog";


interface InternalDeviceEditPopupProps {
  name: string
  deviceType: DeviceType
  internalDeviceForm: DeviceForm | null
  open: boolean
  onClose: () => void
  onSaveAndClose: (name: string, deviceForm: DeviceForm) => void

}

export default function InternalDeviceEditPopup(props: InternalDeviceEditPopupProps) {

  const { name, internalDeviceForm, open, onClose, deviceType, onSaveAndClose } = props

  const [deviceForm, setDeviceForm] = useState(new DeviceForm())

  useEffect(() => {
    if (internalDeviceForm) {
      setDeviceForm(new DeviceForm(internalDeviceForm.id, internalDeviceForm.name, internalDeviceForm.type || deviceType, internalDeviceForm.wires,
        internalDeviceForm.config, internalDeviceForm.internalDevices, internalDeviceForm.referencedDeviceId,
        internalDeviceForm.roomName, internalDeviceForm.pointName, internalDeviceForm.uuid))
    }
  }, [deviceType, internalDeviceForm]);

  const handleDeviceChange = (deviceForm: DeviceForm) => {
    setDeviceForm(deviceForm)
  }

  return (
    <Dialog open={open} onClose={onClose} aria-labelledby="form-dialog-title">
      <DialogTitle id="form-dialog-title">Internal device: {name}</DialogTitle>
      <DialogContent>
        <DeviceDetailsForm deviceForm={deviceForm} onPropertyChange={handleDeviceChange} isInternalDevice={true} internalDeviceType={deviceType} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          Cancel
        </Button>
        <Button onClick={() => { onSaveAndClose(name, deviceForm) }} color="primary">
          Save
        </Button>
      </DialogActions>
    </Dialog>
  )

}