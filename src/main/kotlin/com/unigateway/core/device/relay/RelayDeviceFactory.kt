package com.unigateway.core.device.relay

import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceFactory
import com.unigateway.core.device.MissingConnectorInDeviceConfigurationException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.provider.InputOutputProvider

class RelayDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<RelayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.RELAY
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): RelayDevice {
    val triggerLevel =
      deviceConfiguration.config[RelayDevice.CONFIG_CLOSED_STATE_KEY]?.let { BinaryState.valueOf(it) } ?: RelayDevice.CONFIG_CLOSED_STATE_DEFAULT
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryOutput = ioProvider.getBinaryOutput(connector)
    return RelayDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryOutput, triggerLevel, deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
