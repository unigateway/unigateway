package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.pi4j.io.gpio.GpioPinDigitalInput

class SwitchButtonDevice(
  id: String,
  private val pin: GpioPinDigitalInput,
  debounceMs: Int = CONFIG_DEBOUNCE_DEFAULT
) : DigitalInputDevice(id, DeviceType.SWITCH_BUTTON, pin, debounceMs) {

  override fun initDevice() {
    super.initDevice()
    pin.setDebounce(debounceMs)
  }

  override fun updatableProperty() = STATE
  override fun highStateValue() = RELEASED_STATE_VALUE
  override fun lowStateValue() = PRESSED_STATE_VALUE

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    private const val PRESSED_STATE_VALUE = "PRESSED"
    private const val RELEASED_STATE_VALUE = "RELEASED"
  }
}
