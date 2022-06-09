package com.unigateway.core.device.switchbutton

import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceFactory
import com.unigateway.core.device.MissingConnectorInDeviceConfigurationException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.io.provider.InputOutputProvider

class SwitchButtonDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<SwitchButtonDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.SWITCH_BUTTON
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): SwitchButtonDevice {
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
