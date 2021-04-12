package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import mu.KotlinLogging
import kotlin.concurrent.thread

private val LOGGER = KotlinLogging.logger {}

class EmulatedSwitchButtonDevice(
  id: String,
  pin: MqGpioPinDigitalOutput,
  private val timeBeforeReleaseInMs: Long = DEFAULT_TIME_BEFORE_RELEASE_IN_MS
) :
  DigitalOutputDevice(id, DeviceType.EMULATED_SWITCH, pin) {

  private fun changeState(newState: EmulatedSwitchState) {
    val newPinState = if (newState == EmulatedSwitchState.PRESSED) PRESSED_STATE else RELEASED_STATE
    pin.setState(newPinState)
  }

  fun shortPress(blocking: Boolean = false) {
    LOGGER.debug { "Emulating button $id short press" }
    changeState(EmulatedSwitchState.PRESSED)
    notify(STATE, PRESSED_STATE_VALUE)
    val thread = thread {
      Thread.sleep(timeBeforeReleaseInMs)
      changeState(EmulatedSwitchState.RELEASED)
      notify(STATE, RELEASED_STATE_VALUE)
    }
    if (blocking) {
      thread.join()
    }
  }

  override fun change(propertyId: String, newValue: String) {
    LOGGER.debug { "Changing state on emulated switch $id to $newValue" }
    if (newValue == PRESSED_STATE_VALUE) {
      shortPress()
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
    const val DEFAULT_TIME_BEFORE_RELEASE_IN_MS = 500L
  }
}
