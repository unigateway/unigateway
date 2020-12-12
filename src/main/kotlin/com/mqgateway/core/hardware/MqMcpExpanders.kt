package com.mqgateway.core.hardware

import com.mqgateway.core.gatewayconfig.WireColor
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

interface MqMcpExpanders {
  fun getByPort(portNumber: Int): MqGpioProvider
}

interface MqExpanderPinProvider {
  fun pinDigitalOutput(portNumber: Int, wireColor: WireColor, name: String, defaultState: PinState = PinState.HIGH): MqGpioPinDigitalOutput
  fun pinDigitalInput(
    portNumber: Int,
    wireColor: WireColor,
    name: String,
    resistance: PinPullResistance = PinPullResistance.PULL_UP
  ): MqGpioPinDigitalInput
}
