package com.mqgateway.core.device.motiondetector

import com.mqgateway.core.device.DigitalInputDevice
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryState

class MotionSensorDevice(
  id: String,
  state: BinaryInput,
  private val motionSignalLevel: BinaryState
) : DigitalInputDevice(id, DeviceType.MOTION_DETECTOR, state) {

  override fun updatableProperty() = STATE
  override fun highStateValue() = if (motionSignalLevel == BinaryState.HIGH) MOVE_START_STATE_VALUE else MOVE_STOP_STATE_VALUE
  override fun lowStateValue() = if (motionSignalLevel == BinaryState.HIGH) MOVE_STOP_STATE_VALUE else MOVE_START_STATE_VALUE

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    const val CONFIG_MOTION_SIGNAL_LEVEL_KEY = "motionSignalLevel"
    val CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT = BinaryState.HIGH

    const val MOVE_START_STATE_VALUE = "ON"
    const val MOVE_STOP_STATE_VALUE = "OFF"
  }
}
