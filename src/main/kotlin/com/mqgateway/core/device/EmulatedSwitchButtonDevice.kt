package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import mu.KotlinLogging
import kotlin.concurrent.thread

private val LOGGER = KotlinLogging.logger {}

class EmulatedSwitchButtonDevice(id: String, pin: GpioPinDigitalOutput) : DigitalOutputDevice(id, DeviceType.EMULATED_SWITCH, pin) {

  private fun changeState(newState: EmulatedSwitchState) {
    val newPinState = if (newState == EmulatedSwitchState.PRESSED) PRESSED_STATE else RELEASED_STATE
    pin.state = newPinState
  }

  override fun change(propertyId: String, newValue: String) {
    LOGGER.debug { "Changing state on emulated switch $id to $newValue" }
    if (newValue == PRESSED_STATE_VALUE) {
      changeState(EmulatedSwitchState.PRESSED)
      notify(STATE, newValue)
      thread {
        Thread.sleep(TIME_BEFORE_RELEASE_IN_MS)
        changeState(EmulatedSwitchState.RELEASED)
        notify(STATE, RELEASED_STATE_VALUE)
      }
    }
  }

  enum class EmulatedSwitchState {
    PRESSED, RELEASED
  }

  companion object {
    const val PRESSED_STATE_VALUE = "PRESSED"
    const val RELEASED_STATE_VALUE = "RELEASED"
    val PRESSED_STATE = PinState.LOW
    val RELEASED_STATE = PinState.getInverseState(PRESSED_STATE)!!
    const val TIME_BEFORE_RELEASE_IN_MS = 500L
  }
}
