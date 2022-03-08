package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.configuration.HardwareInterfaceFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

class MqGatewayHardwareFactory : HardwareInterfaceFactory<MqGatewayConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>?): MqGatewayInputOutputProvider {
    return MqGatewayInputOutputProvider(MqGatewayPlatformConfigurationFactory().create(platformConfiguration ?: emptyMap<String, Any>()))
  }

  override fun hardwareConnectorFactory(): MqGatewayConnectorFactory {
    return MqGatewayConnectorFactory()
  }

  override fun connectorClass(): KClass<MqGatewayConnector> {
    return MqGatewayConnector::class
  }

  @ExperimentalSerializationApi
  @InternalSerializationApi
  override fun connectorSerializer(): KSerializer<MqGatewayConnector> {
    TODO()
  }
}
