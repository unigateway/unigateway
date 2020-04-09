package com.mqgateway.core.device

import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import mu.KotlinLogging
import com.mqgateway.core.device.RelayDevice.RelayState.CLOSED
import com.mqgateway.core.device.RelayDevice.RelayState.OPEN
import com.mqgateway.core.gatewayconfig.DeviceType

private val LOGGER = KotlinLogging.logger {}

class RelayDevice(id: String, pin: GpioPinDigitalOutput) : DigitalOutputDevice(id, DeviceType.RELAY, pin) {

  private fun changeState(newState: RelayState) {
    val newPinState = if (newState == CLOSED) RELAY_CLOSED_STATE else RELAY_OPEN_STATE
    pin.state = newPinState
  }

  override fun changeState(propertyId: String, newValue: String) {
    LOGGER.debug { "Changing state on relay $id to $newValue" }
    if (newValue == "ON") {
      changeState(CLOSED)
    } else {
      changeState(OPEN)
    }
    notify(propertyId, newValue)
  }

  enum class RelayState {
    OPEN, CLOSED
  }

  companion object {
    val RELAY_CLOSED_STATE = PinState.LOW
    val RELAY_OPEN_STATE = PinState.getInverseState(RELAY_CLOSED_STATE)!!
  }

}