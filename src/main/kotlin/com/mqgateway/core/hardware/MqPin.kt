package com.mqgateway.core.hardware

import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

interface MqPin

interface MqGpioPinDigitalStateChangeEvent {
  fun getState(): PinState
}

fun interface MqGpioPinListenerDigital {
  fun handleGpioPinDigitalStateChangeEvent(event: MqGpioPinDigitalStateChangeEvent)
}

interface MqGpioPinDigitalInput {
  fun addListener(listener: MqGpioPinListenerDigital)
  fun setDebounce(debounce: Int)
  fun getState(): PinState
  fun setPullResistance(pull: PinPullResistance)
}

interface MqGpioPinDigitalOutput {
  fun setState(newState: PinState)
  fun low()
  fun high()
}
