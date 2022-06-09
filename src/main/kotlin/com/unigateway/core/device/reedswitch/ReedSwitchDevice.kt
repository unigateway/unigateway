package com.unigateway.core.device.reedswitch

import com.unigateway.core.device.DataType.ENUM
import com.unigateway.core.device.DeviceProperty
import com.unigateway.core.device.DevicePropertyType.STATE
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.DigitalInputDevice
import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryState

class ReedSwitchDevice(
  id: String,
  name: String,
  state: BinaryInput,
  config: Map<String, String> = emptyMap()
) : DigitalInputDevice(
  id, name, DeviceType.REED_SWITCH, state,
  setOf(
    DeviceProperty(STATE, ENUM, "OPEN,CLOSED", retained = true)
  ),
  config
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
