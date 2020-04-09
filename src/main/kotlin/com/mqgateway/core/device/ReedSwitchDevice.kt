package com.mqgateway.core.device

import com.pi4j.io.gpio.GpioPinDigitalInput
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType

class ReedSwitchDevice(
    id: String,
    private val pin: GpioPinDigitalInput,
    debounceMs: Int = CONFIG_DEBOUNCE_DEFAULT
): DigitalInputDevice(id, DeviceType.REED_SWITCH, pin, debounceMs) {

  override fun initDevice() {
    super.initDevice()
    pin.setDebounce(debounceMs)
  }

  override fun updatableProperty() = STATE
  override fun highStateValue() = CLOSED_STATE_VALUE
  override fun lowStateValue() = OPENED_STATE_VALUE

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    private const val CLOSED_STATE_VALUE = "CLOSED"
    private const val OPENED_STATE_VALUE = "OPENED"
  }
}