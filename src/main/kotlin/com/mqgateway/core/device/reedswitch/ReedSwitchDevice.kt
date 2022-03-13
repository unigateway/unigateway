package com.mqgateway.core.device.reedswitch

import com.mqgateway.core.device.DataType.ENUM
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.DigitalInputDevice
import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryState

class ReedSwitchDevice(
  id: String,
  name: String,
  state: BinaryInput
) : DigitalInputDevice(
  id, name, DeviceType.REED_SWITCH, state,
  setOf(
    DeviceProperty(STATE, ENUM, "OPEN,CLOSED", retained = true)
  )
) {

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
