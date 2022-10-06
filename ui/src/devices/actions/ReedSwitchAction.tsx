import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface ReedSwitchActionProps {
  device: Device
}

export default function ReedSwitchAction(props: ReedSwitchActionProps) {

  return (
    <Typography variant="h4" color="textSecondary">
      { props.device.properties.get("state") }
    </Typography>)
}