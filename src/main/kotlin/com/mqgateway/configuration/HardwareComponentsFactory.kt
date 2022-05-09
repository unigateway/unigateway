package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.mqgateway.core.hardware.mqgateway.MqGatewayHardwareFactory
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiHardwareFactory
import com.mqgateway.core.hardware.simulated.SimulatedHardwareFactory
import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider
import com.mqgateway.core.io.provider.MySensorsConnector
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer

@Factory
class HardwareComponentsFactory {

  @Singleton
  fun hardwareInterfaceFactory(gatewaySystemProperties: GatewaySystemProperties): HardwareInterfaceFactory<*> {
    val platformConfig = gatewaySystemProperties.platformConfig ?: emptyMap()
    return when (gatewaySystemProperties.platform) {
      "SIMULATED" -> SimulatedHardwareFactory(platformConfig)
      "MQGATEWAY" -> MqGatewayHardwareFactory(platformConfig)
      "RASPBERRYPI" -> RaspberryPiHardwareFactory(platformConfig)
      else -> throw IllegalStateException()
    }
  }

  @Singleton
  fun hardwareInputOutputProvider(hardwareInterfaceFactory: HardwareInterfaceFactory<*>): HardwareInputOutputProvider<*> {
    return hardwareInterfaceFactory.hardwareInputOutputProvider()
  }

  @Singleton
  fun hardwareConnectorFactory(hardwareInterfaceFactory: HardwareInterfaceFactory<*>): HardwareConnectorFactory<*> {
    return hardwareInterfaceFactory.hardwareConnectorFactory()
  }

  @ExperimentalSerializationApi
  @InternalSerializationApi
  @Singleton
  fun <T : HardwareConnector> serializersModule(hardwareInterfaceFactory: HardwareInterfaceFactory<T>): SerializersModule {
    return SerializersModule {
      polymorphic(Connector::class) {
        subclass(MySensorsConnector::class)
        subclass(hardwareInterfaceFactory.connectorClass(), hardwareInterfaceFactory.connectorClass().serializer())
      }
    }
  }
}
