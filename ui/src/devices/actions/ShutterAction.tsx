import {Device} from "../../MqGatewayMutableTypes";
import {Box, Slider, Typography} from "@material-ui/core";
import {ToggleButton, ToggleButtonGroup} from "@material-ui/lab";
import KeyboardArrowUpIcon from '@material-ui/icons/KeyboardArrowUp';
import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import StopIcon from '@material-ui/icons/Stop';
import * as React from "react";
import {useCallback, useEffect, useState} from "react";


interface ShutterActionProps {
  device: Device
}

export default function ShutterAction(props: ShutterActionProps) {

  const { device } = props
  const position = device.properties.get("position");
  const getPosition = useCallback(() => parseInt(position || "0"), [position])
  const [sliderPosition, setSliderPosition] = useState<number | number[]>(getPosition())

  useEffect(() => {
    if (getPosition() !== sliderPosition) {
      setSliderPosition(getPosition());
    }
  }, [getPosition, sliderPosition, device]);

  const sendNewState = (event: React.MouseEvent<HTMLElement>, action: string) => {
    if (action === "up") {
      device.changeProperty("state", "OPEN")
    } else if (action === "down") {
      device.changeProperty("state", "CLOSE")
    } else if (action === "stop") {
      device.changeProperty("state", "STOP")
    }
  };

  const sendNewPosition = (event: React.ChangeEvent<{}>, value: number | number[]) => {
    device.changeProperty("position", value.toString())
  }

  const handleSliderPositionChange = (event: React.ChangeEvent<{}>, value: number | number[]) => {
    setSliderPosition(value)
  }

  const getState = () => {
    return device.properties.get("state") || "-"
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
      <Slider
        value={sliderPosition}
        min={0}
        max={100}
        onChange={handleSliderPositionChange}
        onChangeCommitted={sendNewPosition}
        disabled={device.isModifiedOrNewDevice}
      />
      <ToggleButtonGroup value={currentButtonsState()} exclusive onChange={sendNewState}>
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