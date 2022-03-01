package com.mqgateway.configuration

import com.mqgateway.core.hardware.raspberrypi.RaspberryPiConnector
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiConnectorFactory
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiInputOutputProvider
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
@Requires(property = "gateway.system.platform", value = "RASPBERRYPI")
class RaspberryPiHardwareFactory {

  @Singleton
  fun hardwareInputOutputProvider(): RaspberryPiInputOutputProvider {
    return RaspberryPiInputOutputProvider()
  }

  @Singleton
  fun hardwareConnectorFactory(): RaspberryPiConnectorFactory {
    return RaspberryPiConnectorFactory()
  }

  @Singleton
  fun serializersModule(): SerializersModule {
    return SerializersModule {
      polymorphic(Connector::class) {
        subclass(RaspberryPiConnector::class)
      }
    }
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return SimulatedSystemInfoProvider()
  }
}
