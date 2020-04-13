package com.mqgateway.core.mcpexpander

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider as Pi4jMcp23017GpioProvider
import com.pi4j.io.gpio.GpioProvider

/**
 * Initializes collection of MCP23017 expanders based on given bus number and addresses in I2c bus
 *
 * @param i2CBusNumber see constants in com.pi4j.io.i2c.I2CBus
 * @param expanderAddresses list of MCP23017 expanders ORDERED in the same way they are connected with ports
 */
class Pi4JMcpExpanders(i2CBusNumber: Int, expanderAddresses: List<Int>) : McpExpanders {

  private val expanders: List<GpioProvider> = expanderAddresses.map { Pi4jMcp23017GpioProvider(i2CBusNumber, it) }

  override fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]
}
