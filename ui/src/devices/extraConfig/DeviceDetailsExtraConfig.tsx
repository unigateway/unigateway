import React from 'react';
import {Box} from "@material-ui/core";
import {DeviceType} from "../../communication/MqgatewayTypes";
import RelayExtraConfig from "./RelayExtraConfig";
import SwitchButtonExtraConfig from "./SwitchButtonExtraConfig";
import MotionDetectorExtraConfig from "./MotionDetectorExtraConfig";
import ReedSwitchExtraConfig from "./ReedSwitchExtraConfig";
import ShutterExtraConfig from "./ShutterExtraConfig";
import GateExtraConfig from "./GateExtraConfig";
import Bme280ExtraConfig from "./Bme280ExtraConfig";
import Dht22ExtraConfig from "./Dht22ExtraConfig";
import {DeviceForm} from "../DeviceDetailsDialog";


interface DeviceDetailsExtraConfigProps {
  deviceForm: DeviceForm
  onConfigChange: (config: Map<string, string>) => void
}

const extraConfig = (type: DeviceType, config: Map<string, string>, onChange: (config: Map<string, string>) => void) => {
  switch (type.toString()) {
    case DeviceType[DeviceType.RELAY]: return (<RelayExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.SWITCH_BUTTON]: return (<SwitchButtonExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.MOTION_DETECTOR]: return (<MotionDetectorExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.REED_SWITCH]: return (<ReedSwitchExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.SHUTTER]: return (<ShutterExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.GATE]: return (<GateExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.BME280]: return (<Bme280ExtraConfig config={config} onConfigChange={onChange} />);
    case DeviceType[DeviceType.DHT22]: return (<Dht22ExtraConfig config={config} onConfigChange={onChange} />);
  }
}

export default function DeviceDetailsExtraConfig(props: DeviceDetailsExtraConfigProps) {

  return (
    <Box>
      {extraConfig(props.deviceForm.type, props.deviceForm.config, props.onConfigChange)}
    </Box>
  )

}
