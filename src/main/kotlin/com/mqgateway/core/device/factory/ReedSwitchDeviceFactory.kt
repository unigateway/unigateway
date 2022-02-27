package com.mqgateway.core.device.factory

import com.mqgateway.core.device.ReedSwitchDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider

private const val STATE_CONNECTOR = "state"

class ReedSwitchDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<ReedSwitchDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.REED_SWITCH
  }

  override fun create(deviceConfiguration: DeviceConfiguration): ReedSwitchDevice {
    val stateBinaryInput = ioProvider.getBinaryInput(deviceConfiguration.connectors[STATE_CONNECTOR]!!)
    return ReedSwitchDevice(deviceConfiguration.id, stateBinaryInput)
  }
}
