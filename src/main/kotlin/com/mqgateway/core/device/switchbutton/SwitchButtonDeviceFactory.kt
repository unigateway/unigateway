package com.mqgateway.core.device.switchbutton

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider

private const val STATE_CONNECTOR = "state"

class SwitchButtonDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<SwitchButtonDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.SWITCH_BUTTON
  }

  override fun create(deviceConfiguration: DeviceConfiguration): SwitchButtonDevice {
    val longPressTimeMs =
      deviceConfiguration.config[SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_KEY]?.toLong() ?: SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_DEFAULT
    val stateBinaryInput = ioProvider.getBinaryInput(deviceConfiguration.connectors[STATE_CONNECTOR]!!)
    return SwitchButtonDevice(deviceConfiguration.id, stateBinaryInput, longPressTimeMs)
  }
}
