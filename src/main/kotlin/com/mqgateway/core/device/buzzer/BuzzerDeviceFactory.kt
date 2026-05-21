package com.mqgateway.core.device.buzzer

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.InputOutputProvider

class BuzzerDeviceFactory(
  private val ioProvider: InputOutputProvider<*>,
) : DeviceFactory<BuzzerDevice> {
  override fun deviceType(): DeviceType {
    return DeviceType.BUZZER
  }

  override fun create(
    deviceConfiguration: DeviceConfiguration,
    devices: Set<Device>,
  ): BuzzerDevice {
    val triggerLevel =
      deviceConfiguration.config[BuzzerDevice.CONFIG_CLOSED_STATE_KEY]?.let { BinaryState.valueOf(it) }
        ?: BuzzerDevice.CONFIG_CLOSED_STATE_DEFAULT
    val connector =
      deviceConfiguration.connectors[STATE_CONNECTOR]
        ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryOutput = ioProvider.getBinaryOutput(connector)
    return BuzzerDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryOutput, triggerLevel, deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
