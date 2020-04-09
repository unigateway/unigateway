package com.mqgateway.core.device

import com.pi4j.io.gpio.GpioPinDigitalInput
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType

class MotionSensorDevice(
    id: String,
    private val pin: GpioPinDigitalInput,
    debounceMs: Int
): DigitalInputDevice(id, DeviceType.MOTION_DETECTOR, pin, debounceMs) {

  override fun initDevice() {
    super.initDevice()
    pin.setDebounce(debounceMs)
  }

  override fun updatableProperty() = STATE
  override fun highStateValue() = MOVE_START_STATE_VALUE
  override fun lowStateValue() = MOVE_STOP_STATE_VALUE

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    private const val MOVE_START_STATE_VALUE = "ON"
    private const val MOVE_STOP_STATE_VALUE = "OFF"
  }
}