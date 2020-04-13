package com.mqgateway.core.mcpexpander

import com.pi4j.io.gpio.GpioProvider

interface McpExpanders {
  fun getByPort(portNumber: Int): GpioProvider
}
