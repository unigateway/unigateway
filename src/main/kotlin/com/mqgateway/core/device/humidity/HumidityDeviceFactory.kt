package com.mqgateway.core.device.humidity

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.io.provider.InputOutputProvider

class HumidityDeviceFactory(
  private val ioProvider: InputOutputProvider<*>,
) : DeviceFactory<HumidityDevice> {
  override fun deviceType(): DeviceType {
    return DeviceType.HUMIDITY
  }

  override fun create(
    deviceConfiguration: DeviceConfiguration,
    devices: Set<Device>,
  ): HumidityDevice {
    val connector =
      deviceConfiguration.connectors[STATE_CONNECTOR]
        ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateInput = ioProvider.getFloatInput(connector)
    return HumidityDevice(
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
