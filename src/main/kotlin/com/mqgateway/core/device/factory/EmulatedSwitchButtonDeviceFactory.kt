package com.mqgateway.core.device.factory

import com.mqgateway.core.device.EmulatedSwitchButtonDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider

private const val STATE_CONNECTOR = "state"

class EmulatedSwitchButtonDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<EmulatedSwitchButtonDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.EMULATED_SWITCH
  }

  override fun create(deviceConfiguration: DeviceConfiguration): EmulatedSwitchButtonDevice {
    val stateBinaryOutput = ioProvider.getBinaryOutput(deviceConfiguration.connectors[STATE_CONNECTOR]!!)
    return EmulatedSwitchButtonDevice(deviceConfiguration.id, stateBinaryOutput)
  }
}
