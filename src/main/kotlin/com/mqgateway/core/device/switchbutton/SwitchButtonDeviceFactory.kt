package com.mqgateway.core.device.switchbutton

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider

class SwitchButtonDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<SwitchButtonDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.SWITCH_BUTTON
  }

  override fun create(deviceConfiguration: DeviceConfiguration): SwitchButtonDevice {
    val longPressTimeMs =
      deviceConfiguration.config[SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_KEY]?.toLong() ?: SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_DEFAULT
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryInput = ioProvider.getBinaryInput(connector)
    return SwitchButtonDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryInput, longPressTimeMs, deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
