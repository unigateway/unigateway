package com.mqgateway.core.hardware

import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

interface MqGpioController { // TODO needs to be removed
  fun provisionDigitalOutputPin(provider: MqGpioProvider, pin: MqPin, name: String, defaultState: PinState): MqGpioPinDigitalOutput
  fun provisionDigitalInputPin(provider: MqGpioProvider, pin: MqPin, name: String, resistance: PinPullResistance): MqGpioPinDigitalInput
}

interface MqGpioProvider
