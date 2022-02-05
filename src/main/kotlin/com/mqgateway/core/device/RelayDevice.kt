package com.mqgateway.core.device

import com.mqgateway.core.device.RelayDevice.RelayState.CLOSED
import com.mqgateway.core.device.RelayDevice.RelayState.OPEN
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.BinaryState
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class RelayDevice(id: String, status: BinaryOutput, private val closedState: BinaryState) : DigitalOutputDevice(id, DeviceType.RELAY, status) {

  override fun initProperty(propertyId: String, value: String) {
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

  override fun change(propertyId: String, newValue: String) {
    LOGGER.debug { "Changing state on relay $id to $newValue" }
    if (newValue == STATE_ON) {
      changeState(CLOSED)
    } else {
      changeState(OPEN)
    }
  }

  private fun closedState(): BinaryState = closedState
  private fun openState(): BinaryState = closedState().inverse()

  enum class RelayState {
    OPEN, CLOSED
  }

  companion object {
    const val CONFIG_CLOSED_STATE_KEY = "triggerLevel"
    val CONFIG_CLOSED_STATE_DEFAULT = BinaryState.LOW

    const val STATE_ON = "ON"
    const val STATE_OFF = "OFF"
  }
}
