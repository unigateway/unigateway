package com.mqgateway.core.hardware.pi4j

import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.hardware.MqGpioPinDigitalStateChangeEvent
import com.mqgateway.core.hardware.MqGpioPinListenerDigital
import com.mqgateway.core.hardware.MqPin
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.Pin
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital

class Pi4JPin(private val pin: Pin) : MqPin {

  companion object {
    fun toPi4J(mqPin: MqPin): Pin {
      return (mqPin as Pi4JPin).pin
    }
  }
}

class Pi4JGpioPinDigitalStateChangeEvent(private val event: GpioPinDigitalStateChangeEvent) : MqGpioPinDigitalStateChangeEvent {
  override fun getState(): PinState = event.state
}

class Pi4JGpioPinDigitalInput(private val gpioPinDigitalInput: GpioPinDigitalInput) : MqGpioPinDigitalInput {
  override fun addListener(listener: MqGpioPinListenerDigital) {
    gpioPinDigitalInput.addListener(
      GpioPinListenerDigital { event ->
        listener.handleGpioPinDigitalStateChangeEvent(Pi4JGpioPinDigitalStateChangeEvent(event))
      }
    )
  }

  override fun setDebounce(debounce: Int) {
    gpioPinDigitalInput.setDebounce(debounce)
  }

  override fun getState(): PinState = gpioPinDigitalInput.state

  override fun setPullResistance(pull: PinPullResistance) {
    gpioPinDigitalInput.pullResistance = pull
  }

  fun getPi4J(): GpioPinDigitalInput = gpioPinDigitalInput
}

class Pi4JGpioPinDigitalOutput(private val gpioPinDigitalOutput: GpioPinDigitalOutput) : MqGpioPinDigitalOutput {
  override fun setState(newState: PinState) {
    gpioPinDigitalOutput.state = newState
  }

  override fun low() { setState(PinState.LOW) }
  override fun high() { setState(PinState.HIGH) }

  fun getPi4J(): GpioPinDigitalOutput = gpioPinDigitalOutput
}
