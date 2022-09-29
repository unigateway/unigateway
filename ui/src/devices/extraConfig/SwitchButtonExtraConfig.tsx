import React from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {Box, FormControl, FormControlLabel, FormLabel, Radio, RadioGroup, Slider, Typography} from "@material-ui/core";


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    formControl: {
      marginBottom: theme.spacing(1),
      minWidth: 240,
    },
  })
);

interface SwitchButtonExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function SwitchButtonExtraConfig(props: SwitchButtonExtraConfigProps) {
  const classes = useStyles();
  const {config, onConfigChange} = props

  const handleDebounceMsChange = (event: React.ChangeEvent<{}>, value: (number | number[])) => {
    const config = {...props.config, debounceMs: value}
    onConfigChange(config)
  }

  const handleLongPressTimeMsChange = (event: React.ChangeEvent<{}>, value: (number | number[])) => {
    const config = {...props.config, longPressTimeMs: value}
    onConfigChange(config)
  }

  const handleHaComponentChange = (event: React.ChangeEvent<HTMLInputElement>, value: string) => {
    const config = {...props.config, haComponent: value}
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
      <Typography id="slider-longpress" gutterBottom>
        Long press time in milliseconds (default: 1000ms)
      </Typography>
      <Slider
        defaultValue={1000}
        aria-labelledby="slider-longpress"
        marks
        value={parseInt(config.get("longPressTimeMs") || "1000")}
        min={100}
        max={3000}
        step={100}
        valueLabelDisplay="auto"
        onChange={handleLongPressTimeMsChange}
      />
      <FormControl component="fieldset" className={classes.formControl} fullWidth>
        <FormLabel component="legend">Home Assistant component</FormLabel>
        <RadioGroup row aria-label="ha component" name="hacomponent" defaultValue="binary_sensor" value={config.get("haComponent")} onChange={handleHaComponentChange}>
          <FormControlLabel value="binary_sensor" control={<Radio color="primary" />} label="binary_sensor" />
          <FormControlLabel value="trigger" control={<Radio color="primary" />} label="trigger" />
          <FormControlLabel value="sensor" control={<Radio color="primary" />} label="sensor" />
        </RadioGroup>
      </FormControl>
    </Box>
  )

}
