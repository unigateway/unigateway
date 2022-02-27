package com.mqgateway.core.device.factory

import com.mqgateway.core.device.RelayDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.InputOutputProvider

private const val STATE_CONNECTOR = "state"

class RelayDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<RelayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.RELAY
  }

  override fun create(deviceConfiguration: DeviceConfiguration): RelayDevice {
    val triggerLevel =
      deviceConfiguration.config[RelayDevice.CONFIG_CLOSED_STATE_KEY]?.let { BinaryState.valueOf(it) } ?: RelayDevice.CONFIG_CLOSED_STATE_DEFAULT
    val stateBinaryOutput = ioProvider.getBinaryOutput(deviceConfiguration.connectors[STATE_CONNECTOR]!!)
    return RelayDevice(deviceConfiguration.id, stateBinaryOutput, triggerLevel)
  }
}
