package com.unigateway.core.device.gate

import com.unigateway.core.device.DataType
import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceProperty
import com.unigateway.core.device.DevicePropertyType.STATE
import com.unigateway.core.device.DeviceType

abstract class GateDevice(
  id: String,
  name: String,
  config: Map<String, String> = emptyMap()
) : Device(
  id, name, DeviceType.GATE,
  setOf(
    DeviceProperty(STATE, DataType.ENUM, "OPEN,CLOSE,STOP", retained = true, settable = true)
  ),
  config
) {

  protected var state: State = State.UNKNOWN
    protected set(value) {
      field = value
      notify(STATE, value.name)
    }

  abstract fun close()

  abstract fun open()

  abstract fun stop(blocking: Boolean = false)

  enum class Command {
    OPEN, CLOSE, STOP
  }

  enum class State {
    OPENING, CLOSING, OPEN, CLOSED, UNKNOWN
  }
}
