import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface HumidityActionProps {
  device: Device
}

export default function HumidityAction(props: HumidityActionProps) {

  return (
    <Typography variant="h5" color="textSecondary">
      <div>Humidity: {props.device.properties.get("humidity") || "-" }%</div>
    </Typography>)
}
