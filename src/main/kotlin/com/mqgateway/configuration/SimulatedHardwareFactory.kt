package com.mqgateway.configuration

import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedConnectorFactory
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.utils.SimulatedSystemInfoProvider
import com.mqgateway.core.utils.SystemInfoProvider
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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
  fun serializersModule(): SerializersModule {
    return SerializersModule {
      polymorphic(Connector::class) {
        subclass(SimulatedConnector::class)
      }
    }
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return SimulatedSystemInfoProvider()
  }
}
