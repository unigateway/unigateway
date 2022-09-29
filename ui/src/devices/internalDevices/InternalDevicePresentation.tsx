import React from 'react';
import {Chip} from "@material-ui/core";
import {DeviceType} from "../../communication/MqgatewayTypes";


interface InternalDevicePresentationProps {
  displayName: string
  name: string
  deviceType: DeviceType
  onClick: (name: string, deviceType: DeviceType) => void
  onDelete: (name: string) => void
  isSet: boolean
}

export default function InternalDevicePresentation(props: InternalDevicePresentationProps) {

  const { displayName, name, deviceType, onClick, onDelete, isSet } = props

  return (<Chip
    label={displayName}
    clickable
    onClick={() => onClick(name, deviceType)}
    onDelete={() => onDelete(name)}
    variant={isSet ? "default" : "outlined"}
  />)

}
