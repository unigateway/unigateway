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
      {device.connectors && <ListItemText>Connectors configured</ListItemText>}
      {device.config && <ListItemText>Additional config</ListItemText>}
    </List>)
}
