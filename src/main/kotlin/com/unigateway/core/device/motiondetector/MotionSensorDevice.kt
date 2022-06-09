package com.unigateway.core.device.motiondetector

import com.unigateway.core.device.DataType.ENUM
import com.unigateway.core.device.DeviceProperty
import com.unigateway.core.device.DevicePropertyType.STATE
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.DigitalInputDevice
import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryState

class MotionSensorDevice(
  id: String,
  name: String,
  state: BinaryInput,
  private val motionSignalLevel: BinaryState,
  config: Map<String, String> = emptyMap()
) : DigitalInputDevice(
  id, name, DeviceType.MOTION_DETECTOR, state,
  setOf(
    DeviceProperty(STATE, ENUM, "ON,OFF", retained = true)
  ),
  config
) {

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
