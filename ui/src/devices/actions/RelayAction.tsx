import {FormControlLabel, FormGroup, Grid, Switch} from "@material-ui/core";
import {Device} from "../../MqGatewayMutableTypes";


interface RelayActionProps {
  device: Device
}

export default function RelayAction(props: RelayActionProps) {

  const {device} = props

  function isOn() {
    return device.properties.get("state") === "ON"
  }

  function toggle(): void {
    let newState
    if (device.properties.get("state") === "OFF") {
      newState = "ON"
    } else {
      newState = "OFF"
    }
    device.changeProperty("state", newState)
  }

  return (
    <FormGroup>
      <Grid component="label" container alignItems="center" spacing={1}>
        <Grid item>OFF</Grid>
        <Grid item>
          <FormControlLabel
            control={<Switch checked={isOn()} onChange={() => {toggle()}} />}
            label="ON"
            disabled={device.isModifiedOrNewDevice}
          />
        </Grid>
      </Grid>
    </FormGroup>)
}