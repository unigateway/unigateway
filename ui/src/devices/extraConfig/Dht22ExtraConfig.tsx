import React from 'react';
import {Box, TextField, Typography} from "@material-ui/core";

interface Dht22ExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function Dht22ExtraConfig(props: Dht22ExtraConfigProps) {
  const {config, onConfigChange} = props

  const handleConfigChange = (keyName: string) => (event: React.ChangeEvent<{ value: string }>) => {
    const config = new Map(props.config)
    config.set(keyName, event.target.value)
    onConfigChange(config)
  }

  return (
    <Box>
      <Typography id="node-id-input">
        MySensors node identifier
      </Typography>
      <TextField id="node-id" aria-labelledby="node-id-input"
                 type="number" InputProps={{ inputProps: { min: 1, max: 256 } }}
                 value={config.get("mySensorsNodeId")}
                 onChange={handleConfigChange("mySensorsNodeId")} variant="outlined" fullWidth />

      <Typography id="humidity-id-input">
        MySensors child sensor identifier for <strong>humidity</strong>
      </Typography>
      <TextField id="humidity-id" aria-labelledby="humidity-id-input" defaultValue="0"
                 type="number" InputProps={{ inputProps: { min: 1, max: 256 } }}
                 value={config.get("humidityChildSensorId")}
                 onChange={handleConfigChange("humidityChildSensorId")} variant="outlined" fullWidth />

      <Typography id="temperature-id-input">
        MySensors child sensor identifier for <strong>temperature</strong>
      </Typography>
      <TextField id="temperature-id" aria-labelledby="temperature-id-input" defaultValue="1"
                 type="number" InputProps={{ inputProps: { min: 1, max: 256 } }}
                 value={config.get("temperatureChildSensorId")}
                 onChange={handleConfigChange("temperatureChildSensorId")} variant="outlined" fullWidth />

      <Typography id="debug-id-input">
        MySensors child sensor identifier for <strong>debug</strong>
      </Typography>
      <TextField id="debug-id" aria-labelledby="debug-id-input" defaultValue="2"
                 type="number" InputProps={{ inputProps: { min: 1, max: 256 } }}
                 value={config.get("debugChildSensorId")}
                 onChange={handleConfigChange("debugChildSensorId")} variant="outlined" fullWidth />
    </Box>
  )

}
