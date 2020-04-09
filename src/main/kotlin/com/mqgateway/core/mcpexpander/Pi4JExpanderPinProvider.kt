package com.mqgateway.core.mcpexpander

import com.pi4j.gpio.extension.mcp.MCP23017Pin
import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.Pin
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import com.mqgateway.core.gatewayconfig.WireColor
import java.lang.IllegalArgumentException

interface ExpanderPinProvider {
  fun pinDigitalOutput(portNumber: Int, wireColor: WireColor, name: String, defaultState: PinState = PinState.HIGH): GpioPinDigitalOutput
  fun pinDigitalInput(portNumber: Int, wireColor: WireColor, name: String, resistance: PinPullResistance = PinPullResistance.PULL_UP): GpioPinDigitalInput
}

class Pi4JExpanderPinProvider(
    private val gpio: GpioController,
    private val mcpExpanders: McpExpanders
) : ExpanderPinProvider {

  override fun pinDigitalOutput(portNumber: Int, wireColor: WireColor, name: String, defaultState: PinState): GpioPinDigitalOutput {
    return gpio.provisionDigitalOutputPin(mcpExpanders.getByPort(portNumber), pinOnMcp(portNumber, wireColor), name, defaultState)
  }

  override fun pinDigitalInput(portNumber: Int, wireColor: WireColor, name: String, resistance: PinPullResistance): GpioPinDigitalInput {
    return gpio.provisionDigitalInputPin(mcpExpanders.getByPort(portNumber), pinOnMcp(portNumber, wireColor), name, resistance)
  }

  private fun pinOnMcp(portNumber: Int, wireColor: WireColor): Pin {
    val expanderPinNumber = ((portNumber - 1) % 4) * 4 + (wireColor.number - 3)
    return when (expanderPinNumber) {
      0 -> MCP23017Pin.GPIO_A0
      1 -> MCP23017Pin.GPIO_A1
      2 -> MCP23017Pin.GPIO_A2
      3 -> MCP23017Pin.GPIO_A3
      4 -> MCP23017Pin.GPIO_A4
      5 -> MCP23017Pin.GPIO_A5
      6 -> MCP23017Pin.GPIO_A6
      7 -> MCP23017Pin.GPIO_A7
      8 -> MCP23017Pin.GPIO_B0
      9 -> MCP23017Pin.GPIO_B1
      10 -> MCP23017Pin.GPIO_B2
      11 -> MCP23017Pin.GPIO_B3
      12 -> MCP23017Pin.GPIO_B4
      13 -> MCP23017Pin.GPIO_B5
      14 -> MCP23017Pin.GPIO_B6
      15 -> MCP23017Pin.GPIO_B7
      else -> throw IllegalArgumentException("Wrong port number or wireColor. Calculated expanderPinNumber cannot be outside of [0,15] range.")
    }

  }
}