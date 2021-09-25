import React from 'react';
import {Box, Slider, Typography} from "@material-ui/core";


interface ReedSwitchExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function ReedSwitchExtraConfig(props: ReedSwitchExtraConfigProps) {
  const {config, onConfigChange} = props

  const handleDebounceMsChange = (event: React.ChangeEvent<{}>, value: (number | number[])) => {
    const config = new Map(props.config)
    config.set("debounceMs", value.toString())
    onConfigChange(config)
  }

  return (
    <Box>
      <Typography id="slider-debounce" gutterBottom>
        Debounce in milliseconds (default: 50ms)
      </Typography>
      <Slider
        defaultValue={50}
        aria-labelledby="slider-debounce"
        marks
        value={parseInt(config.get("debounceMs") || "50")}
        min={0}
        max={300}
        step={10}
        valueLabelDisplay="auto"
        onChange={handleDebounceMsChange}
      />
    </Box>
  )

}
