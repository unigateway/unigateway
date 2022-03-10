package com.mqgateway.core.hardware.mqgateway

import com.diozero.api.I2CConstants
import com.diozero.devices.MCP23017

/**
 * Initializes collection of MCP23017 expanders based on given bus number and addresses in I2c bus
 *
 * @param expanderAddresses list of MCP23017 expanders addresses ORDERED in the same way they are connected with ports
 */
class MqGatewayMcpExpanders(expanderAddresses: List<Int>) {

  private val expanders: List<MCP23017> = expanderAddresses.map { MCP23017(I2CConstants.CONTROLLER_0, it, -1, -1) }

  fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]
}
