package com.mqgateway.core.device.temperature

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.io.provider.InputOutputProvider

class TemperatureDeviceFactory(
  private val ioProvider: InputOutputProvider<*>,
) : DeviceFactory<TemperatureDevice> {
  override fun deviceType(): DeviceType {
    return DeviceType.TEMPERATURE
  }

  override fun create(
    deviceConfiguration: DeviceConfiguration,
    devices: Set<Device>,
  ): TemperatureDevice {
    val connector =
      deviceConfiguration.connectors[STATE_CONNECTOR]
        ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateInput = ioProvider.getFloatInput(connector)
    return TemperatureDevice(
      id = deviceConfiguration.id,
      name = deviceConfiguration.name,
      input = stateInput,
      minUpdateIntervalMillis = deviceConfiguration.config.getOrDefault("minUpdateIntervalMs", "0").toLong(),
      config = deviceConfiguration.config,
    )
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
