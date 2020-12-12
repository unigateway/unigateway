package com.mqgateway.core.hardware.pi4j

import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.hardware.MqGpioProvider
import com.mqgateway.core.hardware.MqMcpExpanders
import com.mqgateway.core.hardware.MqPin
import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider
import com.pi4j.gpio.extension.mcp.MCP23017Pin
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState

/**
 * Initializes collection of MCP23017 expanders based on given bus number and addresses in I2c bus
 *
 * @param i2CBusNumber see constants in com.pi4j.io.i2c.I2CBus
 * @param expanderAddresses list of MCP23017 expanders ORDERED in the same way they are connected with ports
 */
class Pi4JMcpExpanders(i2CBusNumber: Int, expanderAddresses: List<Int>) : MqMcpExpanders {

  private val expanders: List<MqGpioProvider> = expanderAddresses.map { Pi4JMcp23017GpioProvider(MCP23017GpioProvider(i2CBusNumber, it)) }

  override fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]
}

class Pi4JMcp23017GpioProvider(mcp23017GpioProvider: MCP23017GpioProvider) : Pi4JGpioProvider(mcp23017GpioProvider)

class Pi4JExpanderPinProvider(
  private val gpio: Pi4JGpioController,
  private val mcpExpanders: MqMcpExpanders
) : MqExpanderPinProvider {

  override fun pinDigitalOutput(portNumber: Int, wireColor: WireColor, name: String, defaultState: PinState): MqGpioPinDigitalOutput {
    return Pi4JGpioPinDigitalOutput(
      gpio.provisionDigitalOutputPin(
        mcpExpanders.getByPort(portNumber),
        pinOnMcp(portNumber, wireColor),
        name,
        defaultState
      ).getPi4J()
    )
  }

  override fun pinDigitalInput(portNumber: Int, wireColor: WireColor, name: String, resistance: PinPullResistance): MqGpioPinDigitalInput {
    return Pi4JGpioPinDigitalInput(
      gpio.provisionDigitalInputPin(
        mcpExpanders.getByPort(portNumber),
        pinOnMcp(portNumber, wireColor),
        name,
        resistance
      ).getPi4J()
    )
  }

  private fun pinOnMcp(portNumber: Int, wireColor: WireColor): MqPin {
    val expanderPinNumber = ((portNumber - 1) % 4) * 4 + (wireColor.number - 3)
    return when (expanderPinNumber) {
      0 -> Pi4JPin(MCP23017Pin.GPIO_A0)
      1 -> Pi4JPin(MCP23017Pin.GPIO_A1)
      2 -> Pi4JPin(MCP23017Pin.GPIO_A2)
      3 -> Pi4JPin(MCP23017Pin.GPIO_A3)
      4 -> Pi4JPin(MCP23017Pin.GPIO_A4)
      5 -> Pi4JPin(MCP23017Pin.GPIO_A5)
      6 -> Pi4JPin(MCP23017Pin.GPIO_A6)
      7 -> Pi4JPin(MCP23017Pin.GPIO_A7)
      8 -> Pi4JPin(MCP23017Pin.GPIO_B0)
      9 -> Pi4JPin(MCP23017Pin.GPIO_B1)
      10 -> Pi4JPin(MCP23017Pin.GPIO_B2)
      11 -> Pi4JPin(MCP23017Pin.GPIO_B3)
      12 -> Pi4JPin(MCP23017Pin.GPIO_B4)
      13 -> Pi4JPin(MCP23017Pin.GPIO_B5)
      14 -> Pi4JPin(MCP23017Pin.GPIO_B6)
      15 -> Pi4JPin(MCP23017Pin.GPIO_B7)
      else -> throw IllegalArgumentException("Wrong port number or wireColor. Calculated expanderPinNumber cannot be outside of [0,15] range.")
    }
  }
}
