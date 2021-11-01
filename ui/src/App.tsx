import React, {useMemo, useState} from 'react';
import './App.css';
import {Badge, Box, Link, MuiThemeProvider, Typography} from "@material-ui/core";
import {createTheme, makeStyles} from "@material-ui/core/styles";
import clsx from "clsx";
import CssBaseline from "@material-ui/core/CssBaseline";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import Drawer from "@material-ui/core/Drawer";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import Divider from "@material-ui/core/Divider";
import List from "@material-ui/core/List";
import {mainListItems, secondaryListItems} from "./listItems";
import {BrowserRouter as Router, Route, Switch as RouterSwitch} from "react-router-dom";
import Dashboard from "./dashboard/Dashboard";
import Devices from "./devices/Devices";
import Topology from "./topology/Topology";
import Rules from "./rules/Rules";
import Logs from "./logs/Logs";
import PageNameHeader from "./PageNameHeader";
import yaml from 'js-yaml';
import YamlConfig from "./yamlconfig/YamlConfig";
import {GatewayConfiguration as GatewayConfigurationData} from "./communication/MqgatewayTypes";
import {ConnectionState, DeviceStateUpdate, GatewayWS} from "./communication/GatewayWS";
import {GatewayConfiguration} from "./MqGatewayMutableTypes";
import NotificationsMenu from "./notifications/NotificationsMenu";
import AnnouncementOutlinedIcon from '@material-ui/icons/AnnouncementOutlined';
import Notification from "./notifications/Notification";
import SettingsInputComponentIcon from "@material-ui/icons/SettingsInputComponent";
import ApplyConfigurationDialog from "./configurationchange/ApplyConfigurationDialog";
import GatewayRest from "./communication/GatewayRest";
import WebSocketConnectionSnackNotification from "./communication/WebSocketConnectionSnackNotification";
import GatewayConnection from "./communication/GatewayConnection";

const theme = createTheme({
  palette: {
    primary: {
      light: '#63a4ff',
      main: '#1976d2',
      dark: '#004ba0',
      contrastText: '#fff',
    },
    secondary: {
      light: '#fff674',
      main: '#ffc342',
      dark: '#c89300',
      contrastText: '#000',
    },
  },
});

function Copyright() {
  return (
    <Typography variant="body2" color="textSecondary" align="center">
      {'Copyright © '}
      <Link color="inherit" href="https://mqgateway.com/">
        MqGateway
      </Link>{' '}
      {new Date().getFullYear()}
    </Typography>
  );
}

const drawerWidth = 240;

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
  },
  toolbar: {
    paddingRight: 24, // keep right padding when drawer closed
  },
  toolbarIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  appBarShift: {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  menuButton: {
    marginRight: 36,
  },
  menuButtonHidden: {
    display: 'none',
  },
  title: {
    flexGrow: 1,
  },
  drawerPaper: {
    position: 'relative',
    whiteSpace: 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: theme.spacing(7),
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing(9),
    },
  },
  appBarSpacer: theme.mixins.toolbar,
  content: {
    flexGrow: 1,
    height: '100vh',
    overflow: 'auto',
  },
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  },
  paper: {
    padding: theme.spacing(2),
    display: 'flex',
    overflow: 'auto',
    flexDirection: 'column',
  },
  fixedHeight: {
    height: 240,
  },
  logo: {
    fontFamily: "Teko",
    fontWeight: "bold",
    fontStyle: "italic",
    fontSize: "25pt"
  },
  logoMq: {
    color: "#1976d2ff"
  },
  logoGateway: {
    color: "#474747fc"
  },
}));

const gatewayRest = new GatewayRest("")
const gatewaySocket = new GatewayWS(`ws://${window.location.host}/devices/ui`);

const gatewayConnection = new GatewayConnection(gatewayRest, gatewaySocket)

type ConfigChanger = ((oldConfiguration: GatewayConfiguration) => GatewayConfiguration) | GatewayConfiguration
type YamlConfigChanger = ((oldYaml: string) => string) | string
export const GatewayConfigurationContext =
  React.createContext<{gatewayConfiguration: GatewayConfiguration, setGatewayConfiguration: (change: ConfigChanger) => void,
    yamlConfiguration: string, setYamlConfiguration: (change: YamlConfigChanger) => void}>({
      gatewayConfiguration: new GatewayConfiguration("", "", "", []),
      setGatewayConfiguration: () => {},
      yamlConfiguration: "",
      setYamlConfiguration: () => {}
    });

export const GatewayCommunicationContext = React.createContext<{gatewayConnection: GatewayConnection}>({gatewayConnection})

function App() {
  const classes = useStyles();
  const [open, setOpen] = useState(true);
  const handleDrawerOpen = () => {
    setOpen(true);
  };
  const handleDrawerClose = () => {
    setOpen(false);
  };

  const [yamlConfiguration, setYamlConfiguration] = useState("");
  const [gatewayConfiguration, setGatewayConfiguration] = useState(new GatewayConfiguration("", "", "", []))
  const gatewayConfigurationValue = useMemo(() => ({gatewayConfiguration, setGatewayConfiguration, yamlConfiguration, setYamlConfiguration}), [gatewayConfiguration, yamlConfiguration])
  const [anchorForNotifications, setAnchorForNotifications] = useState<null | HTMLElement>(null);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [applyConfigurationDialogOpen, setApplyConfigurationDialogOpen] = React.useState(false);
  const [webSocketConnectionState, setWebSocketConnectionState] = useState(ConnectionState.DISCONNECTED)


  React.useEffect(() => {
    gatewaySocket.onConnected(() => setWebSocketConnectionState(ConnectionState.CONNECTED))
    gatewaySocket.onDisconnected(() => setWebSocketConnectionState(ConnectionState.DISCONNECTED))
    gatewaySocket.onUpdate((update) => deviceStateUpdated(update))

    gatewayConnection.reconnect().then(({yaml, gatewayConfiguration}) => {
      setYamlConfiguration(yaml)
      setGatewayConfiguration(gatewayConfiguration)
    })
  }, []);

  React.useEffect(() => {
    if (gatewayConfiguration.hasAnyChanges()) {
      setNotifications(oldNotifications => {
        if (!oldNotifications.find(it => it.id === Notification.CONFIGURATION_CHANGED_ID)) {
          oldNotifications.push(
            new Notification(Notification.CONFIGURATION_CHANGED_ID, "Devices configuration changed",
              "Configuration has been changed. Click here to apply or revert changes.", () => { setApplyConfigurationDialogOpen(true) }, (<SettingsInputComponentIcon />)))
        }
        return [...oldNotifications]
      })
    } else {
      setNotifications(oldNotifications => {
        return oldNotifications.filter(it => it.id !== Notification.CONFIGURATION_CHANGED_ID)
      })
    }
    setYamlConfiguration(yaml.dump(gatewayConfiguration.toDataObject()))
  }, [gatewayConfiguration])

  React.useEffect(() => {
    if (!yamlConfiguration) {
      return
    }
    const gatewayConfigurationData: GatewayConfigurationData = yaml.load(yamlConfiguration) as GatewayConfigurationData;
    const loadedGatewayConfiguration = GatewayConfiguration.fromData(gatewayConfigurationData,
      (deviceId, propertyId, newValue) => gatewaySocket.updateState(deviceId, propertyId, newValue))
    setGatewayConfiguration(previousGatewayConfiguration => {
      if (previousGatewayConfiguration.hasAnyChanges()) {
        loadedGatewayConfiguration.isModified = true
      }
      loadedGatewayConfiguration.allDevices().forEach(deviceFromNewConfig => {
        const deviceFromPreviousConfig = previousGatewayConfiguration.findDevice(deviceFromNewConfig.id)
        if (deviceFromPreviousConfig) {
          if (JSON.stringify(deviceFromNewConfig.toDataObject()) !== JSON.stringify(deviceFromPreviousConfig.toDataObject()) || deviceFromPreviousConfig.isModifiedOrNewDevice) {
            deviceFromNewConfig.isModifiedOrNewDevice = true
            loadedGatewayConfiguration.isModified = true
          }
          Array.from(deviceFromPreviousConfig.properties).forEach(([propertyId, value]) => {
            deviceFromNewConfig.handlePropertyChange(propertyId, value)
          })
        } else {
          deviceFromNewConfig.isModifiedOrNewDevice = true
          loadedGatewayConfiguration.isModified = true
        }
      })
      const previousConfigurationDevicesIds = previousGatewayConfiguration.allDevices().map(device => device.id)
      const loadedConfigurationDevicesIds = loadedGatewayConfiguration.allDevices().map(device => device.id)
      if (!previousConfigurationDevicesIds.every(deviceId => loadedConfigurationDevicesIds.includes(deviceId))) {
        loadedGatewayConfiguration.isModified = true
      }
      return loadedGatewayConfiguration
    })
  }, [yamlConfiguration])

  const deviceStateUpdated = (update: DeviceStateUpdate) => {
    setGatewayConfiguration(oldGatewayConfiguration => {
      const device = oldGatewayConfiguration.findDevice(update.deviceId)
      if (device) {
        device.handlePropertyChange(update.propertyId, update.newValue)
      }
      const newGatewayConfiguration = new GatewayConfiguration(oldGatewayConfiguration.configVersion, oldGatewayConfiguration.name, oldGatewayConfiguration.mqttHostname, oldGatewayConfiguration.rooms);
      if (oldGatewayConfiguration.hasAnyChanges()) {
        newGatewayConfiguration.isModified = true
      }
      return newGatewayConfiguration
    })
  }

  const handleOpenNotifications = (event: React.MouseEvent<HTMLButtonElement>) => {
    if (notifications.length > 0) {
      setAnchorForNotifications(event.currentTarget);
    }
  }

  const handleCloseNotifications = () => {
    setAnchorForNotifications(null);
  };

  const handleYamlConfigurationChanged = (newYamlConfiguration: string) => {
    setYamlConfiguration(newYamlConfiguration);
  }

  const handleApplyConfigurationDialogClose = () => {
    setApplyConfigurationDialogOpen(false)
  }

  return (
    <GatewayConfigurationContext.Provider value={gatewayConfigurationValue}>
    <GatewayCommunicationContext.Provider value={{gatewayConnection}}>
      <MuiThemeProvider theme={theme}>
      <Router basename={process.env.PUBLIC_URL}>
        <div className={classes.root}>
          <CssBaseline />
          <AppBar position="absolute" className={clsx(classes.appBar, open && classes.appBarShift)}>
            <Toolbar className={classes.toolbar}>
              <IconButton
                edge="start"
                color="inherit"
                aria-label="open drawer"
                onClick={handleDrawerOpen}
                className={clsx(classes.menuButton, open && classes.menuButtonHidden)}
              >
                <MenuIcon />
              </IconButton>
              <Typography component="h1" variant="h6" color="inherit" noWrap className={classes.title}>
                <PageNameHeader />
              </Typography>
              {notifications.length > 0 && (
                <Box>
                  <IconButton aria-label="notifications list" color="inherit" aria-haspopup="true" onClick={handleOpenNotifications} >
                    <Badge badgeContent={notifications.length} color="secondary">
                      <AnnouncementOutlinedIcon />
                    </Badge>
                  </IconButton>
                  <NotificationsMenu anchor={anchorForNotifications} onClose={handleCloseNotifications} notifications={notifications} />
                </Box>
              )}
            </Toolbar>
          </AppBar>
          <Drawer
            variant="permanent"
            classes={{
              paper: clsx(classes.drawerPaper, !open && classes.drawerPaperClose),
            }}
            open={open}
          >
            <div className={classes.toolbarIcon}>
              <Typography className={classes.logo}><span className={classes.logoMq}>Mq</span><span className={classes.logoGateway}>Gateway</span></Typography>
              <IconButton onClick={handleDrawerClose}>
                <ChevronLeftIcon />
              </IconButton>
            </div>
            <Divider />
            <List>
              {mainListItems}
            </List>
            <Divider />
            <List>
              {secondaryListItems}
            </List>
          </Drawer>
          <main className={classes.content}>
            <div className={classes.appBarSpacer} />
            <RouterSwitch>
              <Route path="/topology">
                <Topology />
              </Route>
              <Route path="/devices">
                <Devices />
              </Route>
              <Route path="/rules">
                <Rules />
              </Route>
              <Route path="/logs">
                <Logs />
              </Route>
              <Route path="/yamlconfig">
                <YamlConfig config={yamlConfiguration} onSave={handleYamlConfigurationChanged} />
              </Route>
              <Route path="/">
                <Dashboard />
              </Route>
            </RouterSwitch>
            <ApplyConfigurationDialog open={applyConfigurationDialogOpen} onClose={handleApplyConfigurationDialogClose} />
            <WebSocketConnectionSnackNotification connectionState={webSocketConnectionState} />
            <Box pt={4}>
              <Copyright />
            </Box>
          </main>
        </div>
      </Router>
      </MuiThemeProvider>
    </GatewayCommunicationContext.Provider>
    </GatewayConfigurationContext.Provider>
  );
}

export default App;
