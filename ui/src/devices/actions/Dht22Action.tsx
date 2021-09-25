import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface Dht22ActionProps {
  device: Device
}

export default function Dht22Action(props: Dht22ActionProps) {

  return (
    <Typography variant="h5" color="textSecondary">
      <div>Temperature: {props.device.properties.get("temperature") || "-" }°C</div>
      <div>Humidity: {props.device.properties.get("humidity") || "- " }%</div>
    </Typography>)
}