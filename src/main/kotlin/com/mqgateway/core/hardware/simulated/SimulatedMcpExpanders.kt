package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.hardware.MqGpioController
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.hardware.MqGpioProvider
import com.mqgateway.core.hardware.MqMcpExpanders
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

class SimulatedMcpExpanders(expanderAddresses: List<Int>) : MqMcpExpanders {

  private val expanders: List<MqGpioProvider> = expanderAddresses.map { SimulatedGpioProvider() }

  override fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]
}

class SimulatedExpanderPinProvider(private val gpioController: MqGpioController, private val mcpExpanders: MqMcpExpanders) : MqExpanderPinProvider {
  override fun pinDigitalOutput(portNumber: Int, wireColor: WireColor, name: String, defaultState: PinState): MqGpioPinDigitalOutput {
    return SimulatedGpioPinDigitalOutput(defaultState)
  }

  override fun pinDigitalInput(portNumber: Int, wireColor: WireColor, name: String, resistance: PinPullResistance): MqGpioPinDigitalInput {
    return SimulatedGpioPinDigitalInput(resistance)
  }
}
