package com.mqgateway.core.device.emulatedswitch

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider

class EmulatedSwitchButtonDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<EmulatedSwitchButtonDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.EMULATED_SWITCH
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): EmulatedSwitchButtonDevice {
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryOutput = ioProvider.getBinaryOutput(connector)
    return EmulatedSwitchButtonDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryOutput, config = deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
