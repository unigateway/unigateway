import React from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {Box, FormControl, FormControlLabel, FormLabel, Radio, RadioGroup} from "@material-ui/core";


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    formControl: {
      marginBottom: theme.spacing(1),
      minWidth: 240,
    },
  })
);

interface GateExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function GateExtraConfig(props: GateExtraConfigProps) {
  const classes = useStyles();
  const {config, onConfigChange} = props

  const handleHaDeviceClassChange = (event: React.ChangeEvent<HTMLInputElement>, value: string) => {
    const config = new Map(props.config)
    config.set("haDeviceClass", value)
    onConfigChange(config)
  }

  return (
    <Box>
      <FormControl component="fieldset" className={classes.formControl} fullWidth>
        <FormLabel component="legend">Home Assistant device class</FormLabel>
        <RadioGroup row aria-label="Home Assistant device class" name="ha-device-class-level" defaultValue="garage" value={config.get("motionSignalLevel")} onChange={handleHaDeviceClassChange}>
          <FormControlLabel value="garage" control={<Radio color="primary" />} label="garage" />
          <FormControlLabel value="gate" control={<Radio color="primary" />} label="gate" />
        </RadioGroup>
      </FormControl>
    </Box>
  )

}
