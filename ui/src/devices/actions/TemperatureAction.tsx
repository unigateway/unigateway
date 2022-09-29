import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface TemperatureActionProps {
  device: Device
}

export default function TemperatureAction(props: TemperatureActionProps) {

  return (
    <Typography variant="h5" color="textSecondary">
      <div>Temperature: {props.device.properties.get("temperature") || "-" }°C</div>
    </Typography>)
}
