import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface SwitchButtonActionProps {
  device: Device
}

export default function SwitchButtonAction(props: SwitchButtonActionProps) {

  return (
    <Typography variant="h4" color="textSecondary">
      { props.device.properties.get("state") }
    </Typography>)
}