package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.pi4j.io.gpio.PinState

class ReedSwitchDevice(
  id: String,
  private val pin: MqGpioPinDigitalInput,
  debounceMs: Int = CONFIG_DEBOUNCE_DEFAULT
) : DigitalInputDevice(id, DeviceType.REED_SWITCH, pin, debounceMs) {

  override fun initDevice() {
    super.initDevice()
    pin.setDebounce(debounceMs)
  }

  override fun updatableProperty() = STATE
  override fun highStateValue() = OPEN_STATE_VALUE
  override fun lowStateValue() = CLOSED_STATE_VALUE

  fun isClosed() = state == PinState.LOW
  fun isOpen() = !isClosed()

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    const val CLOSED_STATE_VALUE = "CLOSED"
    const val OPEN_STATE_VALUE = "OPEN"
  }
}
