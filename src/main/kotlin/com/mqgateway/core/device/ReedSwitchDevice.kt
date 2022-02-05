package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryState

class ReedSwitchDevice(
  id: String,
  status: BinaryInput,
  debounceMs: Int = CONFIG_DEBOUNCE_DEFAULT
) : DigitalInputDevice(id, DeviceType.REED_SWITCH, status, debounceMs) {

  override fun updatableProperty() = STATE
  override fun highStateValue() = OPEN_STATE_VALUE
  override fun lowStateValue() = CLOSED_STATE_VALUE

  fun isClosed() = state == BinaryState.LOW
  fun isOpen() = !isClosed()

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    const val CLOSED_STATE_VALUE = "CLOSED"
    const val OPEN_STATE_VALUE = "OPEN"
  }
}
