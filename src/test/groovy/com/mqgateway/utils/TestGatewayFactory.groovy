package com.mqgateway.utils

import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.shutter.ShutterDevice
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import com.mqgateway.core.device.unigateway.UniGatewayDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler

class TestGatewayFactory {

  InputOutputProvider ioProvider = new InputOutputProvider(
    new SimulatedInputOutputProvider(new SimulatedPlatformConfiguration("someValue")),
    new MySensorsInputOutputProvider())
  DeviceFactoryProvider deviceFactoryProvider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  RelayDevice relayDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.RELAY)
      .create(new DeviceConfiguration(id, "Relay ${id}", DeviceType.RELAY, [
        state: new SimulatedConnector(1)
      ])) as RelayDevice
  }

  UniGatewayDevice unigatewayDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.UNIGATEWAY)
      .create(new DeviceConfiguration(id, "Unigateway ${id}", DeviceType.UNIGATEWAY)) as UniGatewayDevice
  }

  SwitchButtonDevice switchButtonDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.SWITCH_BUTTON)
      .create(new DeviceConfiguration(id, "Switch button ${id}", DeviceType.SWITCH_BUTTON, [
        state: new SimulatedConnector(1)
      ])) as SwitchButtonDevice
  }

  static GatewayConfiguration gateway(List<DeviceConfiguration> devices) {
    new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", devices)
  }

}
