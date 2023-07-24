package com.mqgateway.core.hardware.mqgateway.mcp

import com.diozero.api.I2CConstants
import com.diozero.devices.MCP23017
import com.diozero.devices.mcp23xxx.MCP23xxx.INTERRUPT_GPIO_NOT_SET
import com.mqgateway.core.hardware.mqgateway.WireColor
import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput

/**
 * Initializes collection of MCP23017 expanders based on given bus number and addresses in I2c bus
 *
 * @param expanderAddresses list of MCP23017 expanders addresses ORDERED in the same way they are connected with ports
 */
class MqGatewayMcpExpanders(expanderAddresses: List<Int>) {

  private val expanders: List<MqGatewayMcpExpander> = expanderAddresses.map {
    MqGatewayMcpExpander(MCP23017(I2CConstants.CONTROLLER_0, it, INTERRUPT_GPIO_NOT_SET, INTERRUPT_GPIO_NOT_SET))
  }

  fun start() {
    expanders.forEach { it.start() }
  }

  fun getInputPin(portNumber: Int, wireColor: WireColor, debounceMs: Long): BinaryInput {
    return getByPort(portNumber).getInputPin(gpioNumberOnMcp(portNumber, wireColor), debounceMs)
  }

  fun getOutputPin(portNumber: Int, wireColor: WireColor): BinaryOutput {
    return getByPort(portNumber).getOutputPin(gpioNumberOnMcp(portNumber, wireColor))
  }

  private fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]
  private fun gpioNumberOnMcp(portNumber: Int, wireColor: WireColor): Int {
    return ((portNumber - 1) % 4) * 4 + (wireColor.number - 3)
  }
}
