package com.mqgateway.core.hardware.pi4j

import com.mqgateway.core.hardware.MqGpioController
import com.mqgateway.core.hardware.MqGpioProvider
import com.mqgateway.core.hardware.MqPin
import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioProvider
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

class Pi4JGpioController(private val gpioController: GpioController) : MqGpioController {

  override fun provisionDigitalOutputPin(provider: MqGpioProvider, pin: MqPin, name: String, defaultState: PinState): Pi4JGpioPinDigitalOutput {
    val pi4JGpioDigitalOutputPin =
      gpioController.provisionDigitalOutputPin(Pi4JGpioProvider.toPi4J(provider), Pi4JPin.toPi4J(pin), name, defaultState)

    return Pi4JGpioPinDigitalOutput(pi4JGpioDigitalOutputPin)
  }

  override fun provisionDigitalInputPin(provider: MqGpioProvider, pin: MqPin, name: String, resistance: PinPullResistance): Pi4JGpioPinDigitalInput {
    val pi4JGpioPinDigitalInput = gpioController.provisionDigitalInputPin(Pi4JGpioProvider.toPi4J(provider), Pi4JPin.toPi4J(pin), name, resistance)
    return Pi4JGpioPinDigitalInput(pi4JGpioPinDigitalInput)
  }
}

open class Pi4JGpioProvider(private val gpioProvider: GpioProvider) : MqGpioProvider {
  companion object {
    fun toPi4J(mqGpioProvider: MqGpioProvider): GpioProvider {
      return (mqGpioProvider as Pi4JGpioProvider).gpioProvider
    }
  }
}
