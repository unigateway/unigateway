package com.mqgateway.configuration

import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.hardware.MqMcpExpanders
import com.mqgateway.core.hardware.simulated.SimulatedExpanderPinProvider
import com.mqgateway.core.hardware.simulated.SimulatedGpioController
import com.mqgateway.core.hardware.simulated.SimulatedMcpExpanders
import com.mqgateway.core.utils.SimulatedSystemInfoProvider
import com.mqgateway.core.utils.SystemInfoProvider
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Factory
@Requires(property = "gateway.system.platform", value = "SIMULATED")
class SimulatedHardwareFactory {

  @Singleton
  fun mcpExpanders(gatewaySystemProperties: GatewaySystemProperties): MqMcpExpanders {
    val mcpPorts: List<Int> = gatewaySystemProperties.components.mcp23017.getPorts().map { it.toInt(16) }
    return SimulatedMcpExpanders(mcpPorts)
  }

  @Singleton
  fun expanderPinProvider(mcpExpanders: MqMcpExpanders): MqExpanderPinProvider {
    return SimulatedExpanderPinProvider(SimulatedGpioController(), mcpExpanders)
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return SimulatedSystemInfoProvider()
  }
}
