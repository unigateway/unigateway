import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface EnvironmentSensorActionProps {
  device: Device
}

export default function EnvironmentSensorAction(props: EnvironmentSensorActionProps) {

  return (
    <Typography variant="h5" color="textSecondary" align="center">
      <div>Temperature: {props.device.properties.get("temperature") || "-" }°C</div>
      <div>Humidity: {props.device.properties.get("humidity") || "- " }%</div>
      <div>Pressure: {props.device.properties.get("pressure") || "-" } Pa</div>
    </Typography>)
}
