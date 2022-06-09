package com.unigateway.utils

import com.unigateway.core.device.DeviceFactoryProvider
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.unigateway.core.device.reedswitch.ReedSwitchDevice
import com.unigateway.core.device.relay.RelayDevice
import com.unigateway.core.device.switchbutton.SwitchButtonDevice
import com.unigateway.core.device.unigateway.UniGatewayDevice
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.unigateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.unigateway.core.io.TestSerial
import com.unigateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.unigateway.core.io.provider.DisabledMySensorsInputOutputProvider
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.mysensors.MySensorMessageSerializer
import com.unigateway.core.mysensors.MySensorsSerialConnection
import com.unigateway.core.utils.FakeSystemInfoProvider
import com.unigateway.core.utils.TimersScheduler
import com.unigateway.core.device.DeviceFactoryProvider
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.unigateway.core.device.reedswitch.ReedSwitchDevice
import com.unigateway.core.device.relay.RelayDevice
import com.unigateway.core.device.switchbutton.SwitchButtonDevice
import com.unigateway.core.device.unigateway.UniGatewayDevice
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.unigateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.unigateway.core.io.TestSerial
import com.unigateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.mysensors.MySensorMessageSerializer
import com.unigateway.core.mysensors.MySensorsSerialConnection
import com.unigateway.core.utils.FakeSystemInfoProvider
import com.unigateway.core.utils.TimersScheduler

class TestGatewayFactory {

  def serial = new TestSerial()
	MySensorsSerialConnection serialConnection = new MySensorsSerialConnection(serial, new MySensorMessageSerializer())
	InputOutputProvider ioProvider = new InputOutputProvider(
		new SimulatedInputOutputProvider(new SimulatedPlatformConfiguration("someValue")),
		new DefaultMySensorsInputOutputProvider(serialConnection))
	DeviceFactoryProvider deviceFactoryProvider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

	RelayDevice relayDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.RELAY)
      .create(new DeviceConfiguration(id, "Relay ${id}", DeviceType.RELAY, [
        state: new SimulatedConnector(1)
      ]), [] as Set) as RelayDevice
  }

	UniGatewayDevice unigatewayDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.UNIGATEWAY)
      .create(new DeviceConfiguration(id, "Unigateway ${id}", DeviceType.UNIGATEWAY), [] as Set) as UniGatewayDevice
  }

	SwitchButtonDevice switchButtonDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.SWITCH_BUTTON)
      .create(new DeviceConfiguration(id, "Switch button ${id}", DeviceType.SWITCH_BUTTON, [
        state: new SimulatedConnector(1)
      ]), [] as Set) as SwitchButtonDevice
  }

	EmulatedSwitchButtonDevice emulatedSwitchButtonDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.EMULATED_SWITCH)
      .create(new DeviceConfiguration(id, "Emulated switch button ${id}", DeviceType.EMULATED_SWITCH, [
        state: new SimulatedConnector(1)
      ]), [] as Set) as EmulatedSwitchButtonDevice
  }

	ReedSwitchDevice reedSwitchDevice(String id) {
    return deviceFactoryProvider.getFactory(DeviceType.REED_SWITCH)
      .create(new DeviceConfiguration(id, "Reed switch ${id}", DeviceType.REED_SWITCH, [
        state: new SimulatedConnector(1)
      ]), [] as Set) as ReedSwitchDevice
  }

  static GatewayConfiguration gateway(List<DeviceConfiguration> devices) {
    new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", devices)
  }

}
