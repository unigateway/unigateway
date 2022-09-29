import {makeStyles} from "@material-ui/core/styles";
import {DeviceType} from "../communication/MqgatewayTypes";
import RelayAction from "./actions/RelayAction";
import TemperatureAction from "./actions/TemperatureAction";
import HumidityAction from "./actions/HumidityAction";
import SwitchButtonAction from "./actions/SwitchButtonAction";
import ReedSwitchAction from "./actions/ReedSwitchAction";
import MotionDetectorAction from "./actions/MotionDetectorAction";
import ShutterAction from "./actions/ShutterAction";
import GateAction from "./actions/GateAction";
import EmulatedSwitchAction from "./actions/EmulatedSwitchAction";
import {Device} from "../MqGatewayMutableTypes";

const useStyles = makeStyles({
  alignItemsAndJustifyContent: {
    display: 'flex',
    height: "110px",
    alignItems: 'center',
    justifyContent: 'center'
  },
});

interface DeviceCardActionSideProps {
  device: Device
}

function chooseAction(device: Device) {
  switch (device.type.toString()) {
    case DeviceType[DeviceType.RELAY]: return (<RelayAction device={device} />);
    case DeviceType[DeviceType.TEMPERATURE]: return (<TemperatureAction device={device} />);
    case DeviceType[DeviceType.HUMIDITY]: return (<HumidityAction device={device} />);
    case DeviceType[DeviceType.SWITCH_BUTTON]: return (<SwitchButtonAction device={device} />);
    case DeviceType[DeviceType.REED_SWITCH]: return (<ReedSwitchAction device={device} />);
    case DeviceType[DeviceType.MOTION_DETECTOR]: return (<MotionDetectorAction device={device} />);
    case DeviceType[DeviceType.EMULATED_SWITCH]: return (<EmulatedSwitchAction device={device} />);
    // case DeviceType[DeviceType.TIMER_SWITCH]: return (<TimerSwitchAction device={device} />);
    case DeviceType[DeviceType.SHUTTER]: return (<ShutterAction device={device} />);
    case DeviceType[DeviceType.GATE]: return (<GateAction device={device} />);
    default:
      return (<div>Not implemented: {device.type.toString()}</div>)
  }
}

export default function DeviceCardActionSide(props: DeviceCardActionSideProps) {
  const classes = useStyles();
  const device: Device = props.device;

  const action = chooseAction(device);

  return (
    <div className={classes.alignItemsAndJustifyContent}>{action}</div>
  );
}
