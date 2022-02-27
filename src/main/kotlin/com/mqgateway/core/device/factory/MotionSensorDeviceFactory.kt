package com.mqgateway.core.device.factory

import com.mqgateway.core.device.MotionSensorDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.InputOutputProvider

private const val STATE_CONNECTOR = "state"

class MotionSensorDeviceFactory(
  private val ioProvider: InputOutputProvider<*>
) : DeviceFactory<MotionSensorDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.MOTION_DETECTOR
  }

  override fun create(deviceConfiguration: DeviceConfiguration): MotionSensorDevice {
    val motionSignalLevelString = deviceConfiguration.config[MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_KEY]
    val motionSignalLevel = motionSignalLevelString?.let { BinaryState.valueOf(it) } ?: MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT
    val stateBinaryInput = ioProvider.getBinaryInput(deviceConfiguration.connectors[STATE_CONNECTOR]!!)
    return MotionSensorDevice(deviceConfiguration.id, stateBinaryInput, motionSignalLevel)
  }
}
