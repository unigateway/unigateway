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

interface MotionDetectorExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function MotionDetectorExtraConfig(props: MotionDetectorExtraConfigProps) {
  const classes = useStyles();
  const {config, onConfigChange} = props

  const handleDebounceMsChange = (event: React.ChangeEvent<{}>, value: (number | number[])) => {
    const config = new Map(props.config)
    config.set("debounceMs", value.toString())
    onConfigChange(config)
  }

  const handleMotionSignalLevelChange = (event: React.ChangeEvent<HTMLInputElement>, value: string) => {
    const config = new Map(props.config)
    config.set("motionSignalLevel", value)
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
      <FormControl component="fieldset" className={classes.formControl} fullWidth>
        <FormLabel component="legend">Motion signal level</FormLabel>
        <RadioGroup row aria-label="motion signal level" name="motion-signal-level" defaultValue="HIGH" value={config.get("motionSignalLevel")} onChange={handleMotionSignalLevelChange}>
          <FormControlLabel value="HIGH" control={<Radio color="primary" />} label="HIGH" />
          <FormControlLabel value="LOW" control={<Radio color="primary" />} label="LOW" />
        </RadioGroup>
      </FormControl>
    </Box>
  )

}
