import {Device} from "../../MqGatewayMutableTypes";
import {Typography} from "@material-ui/core";
import TouchAppIcon from '@material-ui/icons/TouchApp';
import {ToggleButton} from "@material-ui/lab";


interface EmulatedSwitchActionProps {
  device: Device
}

export default function EmulatedSwitchAction(props: EmulatedSwitchActionProps) {

  const { device } = props

  const handleMouseDown = () => {
    props.device.changeProperty("state", "PRESSED")
  };

  const getState = () => {
    return props.device.properties.get("state") || "-"
  }

  return (
    <Typography align="center">
      <ToggleButton aria-label="list" onMouseDown={handleMouseDown} value={false} selected={getState() === "PRESSED"} disabled={device.isModifiedOrNewDevice}>
        <TouchAppIcon />
      </ToggleButton>
    </Typography>
  )
}