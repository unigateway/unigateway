import {List, ListItemText} from "@material-ui/core";
import {Device} from "../MqGatewayMutableTypes";

interface DeviceCardConfigSideProps {
  device: Device
}

export default function DeviceCardConfigSide(props: DeviceCardConfigSideProps) {
  const device: Device = props.device;

  return (
    <List dense>
      <ListItemText>ID: {device.id}</ListItemText>
      <ListItemText>Type: {device.type}</ListItemText>
      {device.wires && <ListItemText>Wires: {device.wires.join(", ")}</ListItemText>}
      {device.internalDevices.size > 0 && <ListItemText>Internal devices</ListItemText>}
      {device.referencedDeviceId && <ListItemText>Referenced ID: {device.referencedDeviceId}</ListItemText>}
      {device.config && <ListItemText>Additional config</ListItemText>}
    </List>)
}