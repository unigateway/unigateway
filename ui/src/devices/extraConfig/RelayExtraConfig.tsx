import React from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {Box, FormControl, FormControlLabel, FormLabel, Radio, RadioGroup} from "@material-ui/core";


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    formControl: {
      margin: theme.spacing(1),
      minWidth: 240,
    },
  })
);

interface RelayExtraConfigProps {
  config: Map<string, string>
  onConfigChange: (config: Map<string, string>) => void
}

export default function RelayExtraConfig(props: RelayExtraConfigProps) {
  const classes = useStyles();

  const {config, onConfigChange} = props

  const handleConfigChange = (keyName: string) => (event: React.ChangeEvent<HTMLInputElement>, value: string) => {
    const config = new Map(props.config)
    config.set(keyName, value)
    onConfigChange(config)
  }

  return (
    <Box>
      <FormControl component="fieldset" className={classes.formControl} fullWidth>
        <FormLabel component="legend">Trigger level</FormLabel>
        <RadioGroup row aria-label="trigger level" name="trigger-level" defaultValue="LOW" value={config.get("triggerLevel")} onChange={handleConfigChange("triggerLevel")}>
          <FormControlLabel value="LOW" control={<Radio color="primary" />} label="LOW" />
          <FormControlLabel value="HIGH" control={<Radio color="primary" />} label="HIGH" />
        </RadioGroup>
      </FormControl>
      <FormControl component="fieldset" className={classes.formControl} fullWidth>
        <FormLabel component="legend">Home Assistant component</FormLabel>
        <RadioGroup row aria-label="ha component" name="hacomponent" defaultValue="switch" value={config.get("haComponent")} onChange={handleConfigChange("haComponent")}>
          <FormControlLabel value="switch" control={<Radio color="primary" />} label="switch" />
          <FormControlLabel value="light" control={<Radio color="primary" />} label="light" />
        </RadioGroup>
      </FormControl>
    </Box>
  )

}
