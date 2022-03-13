package com.mqgateway.core.device.motiondetector

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.InputOutputProvider

class MotionSensorDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<MotionSensorDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.MOTION_DETECTOR
  }

  override fun create(deviceConfiguration: DeviceConfiguration): MotionSensorDevice {
    val motionSignalLevelString = deviceConfiguration.config[MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_KEY]
    val motionSignalLevel = motionSignalLevelString?.let { BinaryState.valueOf(it) } ?: MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryInput = ioProvider.getBinaryInput(connector)
    return MotionSensorDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryInput, motionSignalLevel)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
