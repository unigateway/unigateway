package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.pi4j.io.gpio.PinState

class MotionSensorDevice(
  id: String,
  private val pin: MqGpioPinDigitalInput,
  debounceMs: Int,
  private val motionSignalLevel: PinState
) : DigitalInputDevice(id, DeviceType.MOTION_DETECTOR, pin, debounceMs) {

  override fun initDevice() {
    super.initDevice()
    pin.setDebounce(debounceMs)
  }

  override fun updatableProperty() = STATE
  override fun highStateValue() = if (motionSignalLevel == PinState.HIGH) MOVE_START_STATE_VALUE else MOVE_STOP_STATE_VALUE
  override fun lowStateValue() = if (motionSignalLevel == PinState.HIGH) MOVE_STOP_STATE_VALUE else MOVE_START_STATE_VALUE

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    const val CONFIG_MOTION_SIGNAL_LEVEL_KEY = "motionSignalLevel"
    val CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT = PinState.HIGH

    const val MOVE_START_STATE_VALUE = "ON"
    const val MOVE_STOP_STATE_VALUE = "OFF"
  }
}
