import React, {useContext, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import {Box, Fab, Grid, InputAdornment, TextField} from "@material-ui/core";
import SearchIcon from '@material-ui/icons/Search';
import DeviceCard from "./DeviceCard";
import FlipIcon from '@material-ui/icons/Flip';
import {GatewayConfigurationContext} from "../App";
import {Device} from "../MqGatewayMutableTypes";

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      display: 'flex',
      flexWrap: 'wrap',
    },
    textField: {
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(1),
      width: '25ch',
    },
    container: {
      paddingTop: theme.spacing(4),
      paddingBottom: theme.spacing(4),
    },
    searchBox: {
      backgroundColor: 'white',
      padding: '15px',
      margin: 0
    },
    fabs: {
      position: 'absolute',
      bottom: theme.spacing(5),
      right: theme.spacing(8),
    },
    leftFab: {
      right: theme.spacing(1),
    },
  }),
);

export default function Devices() {
  const classes = useStyles();
  const { gatewayConfiguration } = useContext(GatewayConfigurationContext)
  const devices = gatewayConfiguration.allDevices()
  const [filterPhrase, setFilterPhrase] = useState("")
  const [deviceCardSide, setDeviceCardSide] = React.useState<"config" | "action">("action");

  const toggleActionConfigSide = () => {
    if (deviceCardSide === "config") {
      setDeviceCardSide("action")
    } else {
      setDeviceCardSide("config")
    }
  }

  return (
    <Box>
      <Box className={classes.searchBox}>
        <TextField
          id="search-devices-input"
          value={filterPhrase}
          onChange={(event: any) => setFilterPhrase(event.target.value)}
          placeholder="Filter"
          fullWidth
          InputProps={{
            startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
          }}
        />
      </Box>
      <Container maxWidth="xl" className={classes.container}>
        <Grid container spacing={3}>
          {devices.filter(device => device.name.toLowerCase().includes(filterPhrase.toLowerCase())).map((device: Device) => {
            return (<Grid item key={device.id} xs={12} md={6} lg={4} xl={3}><DeviceCard device={device} side={deviceCardSide} /></Grid>)
          })}

        </Grid>

        <div className={classes.fabs}>
          <Fab aria-label="Flip" className={classes.leftFab} color="secondary" size="large" onClick={toggleActionConfigSide}>
            <FlipIcon />
          </Fab>
        </div>

      </Container>
    </Box>
  );
}
