package com.mqgateway.core.hardware

import com.mqgateway.configuration.GatewaySystemProperties
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

val DEFAULT_PLATFORM = GatewaySystemProperties.SystemPlatform.NANOPI

interface MqGpioController {
  fun provisionDigitalOutputPin(provider: MqGpioProvider, pin: MqPin, name: String, defaultState: PinState): MqGpioPinDigitalOutput
  fun provisionDigitalInputPin(provider: MqGpioProvider, pin: MqPin, name: String, resistance: PinPullResistance): MqGpioPinDigitalInput
}

interface MqGpioProvider
