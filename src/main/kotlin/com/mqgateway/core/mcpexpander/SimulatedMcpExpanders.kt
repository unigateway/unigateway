package com.mqgateway.core.mcpexpander

import com.pi4j.io.gpio.GpioProvider
import com.pi4j.io.gpio.SimulatedGpioProvider

class SimulatedMcpExpanders(expanderAddresses: List<Int>): McpExpanders {

  private val expanders: List<GpioProvider> = expanderAddresses.map { SimulatedGpioProvider() }

  override fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]

}