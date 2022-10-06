import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";


interface MotionDetectorActionProps {
  device: Device
}

export default function MotionDetectorAction(props: MotionDetectorActionProps) {

  const motionDescription = (sensorValue: string) => {
    if (sensorValue === "ON") {
      return "DETECTED"
    } else if (sensorValue === "OFF") {
      return "CLEAR"
    } else {
      return "UNKNOWN"
    }
  }

  return (
    <Typography variant="h4" color="textSecondary">
      { motionDescription(props.device.properties.get("state") || "unknown" ) }
    </Typography>)
}