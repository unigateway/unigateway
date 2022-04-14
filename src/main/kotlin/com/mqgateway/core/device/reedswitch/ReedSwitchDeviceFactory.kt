package com.mqgateway.core.device.reedswitch

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider

class ReedSwitchDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<ReedSwitchDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.REED_SWITCH
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): ReedSwitchDevice {
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryInput = ioProvider.getBinaryInput(connector)
    return ReedSwitchDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryInput, deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
