package com.mqgateway.configuration

import com.mqgateway.core.hardware.simulated.SimulatedConnectorFactory
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.utils.SimulatedSystemInfoProvider
import com.mqgateway.core.utils.SystemInfoProvider
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Factory
@Requires(property = "gateway.system.platform", value = "SIMULATED")
class SimulatedHardwareFactory {

  @Singleton
  fun hardwareInputOutputProvider(): SimulatedInputOutputProvider {
    return SimulatedInputOutputProvider()
  }

  @Singleton
  fun hardwareConnectorFactory(): SimulatedConnectorFactory {
    return SimulatedConnectorFactory()
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return SimulatedSystemInfoProvider()
  }
}
