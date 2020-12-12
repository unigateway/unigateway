package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.hardware.MqGpioController
import com.mqgateway.core.hardware.MqGpioProvider
import com.mqgateway.core.hardware.MqPin
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

class SimulatedGpioController : MqGpioController {
  override fun provisionDigitalOutputPin(provider: MqGpioProvider, pin: MqPin, name: String, defaultState: PinState):
    SimulatedGpioPinDigitalOutput {
      return SimulatedGpioPinDigitalOutput(defaultState)
    }

  override fun provisionDigitalInputPin(
    provider: MqGpioProvider,
    pin: MqPin,
    name: String,
    resistance: PinPullResistance
  ): SimulatedGpioPinDigitalInput {
    return SimulatedGpioPinDigitalInput(resistance)
  }
}

class SimulatedGpioProvider : MqGpioProvider
