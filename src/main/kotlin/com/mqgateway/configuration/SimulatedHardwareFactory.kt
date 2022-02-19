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
@Requires(property = "gateway.system.platform", value = "MQGATEWAY") // TODO this class needs to be removed completely
class SimulatedHardwareFactory {

  @Singleton
  fun mcpExpanders(): MqMcpExpanders { // TODO needs to be changed or removed
    val mcpPorts: List<Int> = listOf("20", "21", "22", "23", "24", "25", "26", "27").map { it.toInt(16) }
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
