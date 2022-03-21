package com.mqgateway.core.device.relay

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.InputOutputProvider

class RelayDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<RelayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.RELAY
  }

  override fun create(deviceConfiguration: DeviceConfiguration): RelayDevice {
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
