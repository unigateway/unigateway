package com.unigateway.core.device.humidity

import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceFactory
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.MissingConnectorInDeviceConfigurationException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.io.provider.InputOutputProvider

class HumidityDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<HumidityDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.HUMIDITY
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): HumidityDevice {
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateInput = ioProvider.getFloatInput(connector)
    return HumidityDevice(deviceConfiguration.id, deviceConfiguration.name, stateInput, deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
