import {Box, Typography} from "@material-ui/core";
import {ToggleButton, ToggleButtonGroup} from "@material-ui/lab";
import KeyboardArrowUpIcon from '@material-ui/icons/KeyboardArrowUp';
import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import StopIcon from '@material-ui/icons/Stop';
import {Device} from "../../MqGatewayMutableTypes";


interface GateActionProps {
  device: Device
}

export default function GateAction(props: GateActionProps) {

  const { device } = props

  const handleChange = (event: React.MouseEvent<HTMLElement>, action: string) => {
    if (action === "up") {
      props.device.changeProperty("state", "OPEN")
    } else if (action === "down") {
      props.device.changeProperty("state", "CLOSE")
    } else if (action === "stop") {
      props.device.changeProperty("state", "STOP")
    }
  };

  const getState = () => {
    return props.device.properties.get("state") || "-"
  }

  const currentButtonsState = () => {
    if (getState() === "OPENING") {
      return "up";
    } else if (getState() === "CLOSING") {
      return "down";
    } else {
      return "";
    }
  }

  return (
    <Box alignItems="center">
      <ToggleButtonGroup value={currentButtonsState()} exclusive onChange={handleChange}>
        <ToggleButton value="down" aria-label="quilt" disabled={device.isModifiedOrNewDevice}>
          <KeyboardArrowDownIcon />
        </ToggleButton>
        <ToggleButton value="stop" aria-label="module" disabled={device.isModifiedOrNewDevice}>
          <StopIcon />
        </ToggleButton>
        <ToggleButton value="up" aria-label="list" disabled={device.isModifiedOrNewDevice}>
          <KeyboardArrowUpIcon />
        </ToggleButton>
      </ToggleButtonGroup>
      <Typography align="center">{getState()}</Typography>
    </Box>
  )
}