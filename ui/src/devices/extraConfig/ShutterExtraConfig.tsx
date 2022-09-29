import React from 'react';
import {Box, TextField, Typography} from "@material-ui/core";


interface ShutterExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function ShutterExtraConfig(props: ShutterExtraConfigProps) {
  const {config, onConfigChange} = props

  const handleConfigChange = (keyName: string) => (event: React.ChangeEvent<{ value: string }>) => {
    const config = new Map(props.config)
    config.set(keyName, event.target.value)
    onConfigChange(config)
  }

  return (
    <Box>
      <Typography id="full-close-input">
        Time to <b>fully close</b> the shutter in milliseconds (required)
      </Typography>
      <TextField id="full-close" aria-labelledby="full-close-input"
                 type="number" InputProps={{ inputProps: { min: 1000, max: 180000, step: 10 } }}
                 value={config.get("fullCloseTimeMs")}
                 onChange={handleConfigChange("fullCloseTimeMs")} variant="outlined" fullWidth />
      <Typography id="full-open-input">
        Time to <b>fully open</b> the shutter in milliseconds (required)
      </Typography>
      <TextField id="full-open" aria-labelledby="full-open-input"
                 type="number" InputProps={{ inputProps: { min: 1000, max: 180000, step: 10 } }}
                 value={config.get("fullOpenTimeMs")}
                 onChange={handleConfigChange("fullOpenTimeMs")} variant="outlined" fullWidth />
    </Box>
  )

}
