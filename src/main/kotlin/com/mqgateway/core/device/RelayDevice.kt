package com.mqgateway.core.device

import com.mqgateway.core.device.RelayDevice.RelayState.CLOSED
import com.mqgateway.core.device.RelayDevice.RelayState.OPEN
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class RelayDevice(id: String, pin: MqGpioPinDigitalOutput, private val triggerLevel: PinState) : DigitalOutputDevice(id, DeviceType.RELAY, pin) {

  override fun initProperty(propertyId: String, value: String) {
    if (propertyId != STATE.toString()) {
      LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
      return
    }
    change(STATE.toString(), value)
  }

  fun changeState(newState: RelayState) {
    if (newState == CLOSED) {
      pin.setState(closedState())
      notify(STATE, STATE_ON)
    } else {
      pin.setState(openState())
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

  private fun closedState(): PinState = triggerLevel
  private fun openState(): PinState = PinState.getInverseState(closedState())!!

  enum class RelayState {
    OPEN, CLOSED
  }

  companion object {
    const val CONFIG_TRIGGER_LEVEL_KEY = "triggerLevel"
    val CONFIG_TRIGGER_LEVEL_DEFAULT = PinState.LOW

    const val STATE_ON = "ON"
    const val STATE_OFF = "OFF"
  }
}
