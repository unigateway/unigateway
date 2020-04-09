package com.mqgateway.core.mcpexpander

import com.pi4j.platform.Platform
import com.pi4j.platform.PlatformManager

object McpExpandersFactory {

  fun create(i2CBusNumber: Int, expanderAddresses: List<Int>): McpExpanders {
    return if (PlatformManager.getPlatform() == Platform.SIMULATED) {
      SimulatedMcpExpanders(expanderAddresses)
    } else {
      Pi4JMcpExpanders(i2CBusNumber, expanderAddresses)
    }
  }
}