package com.mqgateway.core.device.relay

import com.mqgateway.core.device.DataType.ENUM
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.DigitalOutputDevice
import com.mqgateway.core.device.relay.RelayDevice.RelayState.CLOSED
import com.mqgateway.core.device.relay.RelayDevice.RelayState.OPEN
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class RelayDevice(id: String, name: String, state: BinaryOutput, private val closedState: BinaryState, config: Map<String, String> = emptyMap()) :
  DigitalOutputDevice(
    id,
    name,
    DeviceType.RELAY,
    state,
    setOf(
      DeviceProperty(STATE, ENUM, "ON,OFF", settable = true, retained = true),
    ),
    config,
  ) {
  override fun initProperty(
    propertyId: String,
    value: String,
  ) {
    if (propertyId != STATE.toString()) {
      LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
      return
    }
    change(STATE.toString(), value)
  }

  fun changeState(newState: RelayState) {
    if (newState == CLOSED) {
      binaryOutput.setState(closedState())
      notify(STATE, STATE_ON)
    } else {
      binaryOutput.setState(openState())
      notify(STATE, STATE_OFF)
    }
  }

  override fun change(
    propertyId: String,
    newValue: String,
  ) {
    LOGGER.debug { "Changing state on relay $id to $newValue" }
    if (newValue == STATE_ON) {
      changeState(CLOSED)
    } else {
      changeState(OPEN)
    }
  }

  private fun closedState(): BinaryState = closedState

  private fun openState(): BinaryState = closedState().invert()

  enum class RelayState {
    OPEN,
    CLOSED,
  }

  companion object {
    const val CONFIG_CLOSED_STATE_KEY = "triggerLevel"
    val CONFIG_CLOSED_STATE_DEFAULT = BinaryState.LOW

    const val STATE_ON = "ON"
    const val STATE_OFF = "OFF"
  }
}
