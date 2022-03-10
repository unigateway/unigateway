package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.configuration.HardwareInterfaceFactory
import kotlin.reflect.KClass

class MqGatewayHardwareFactory : HardwareInterfaceFactory<MqGatewayConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>): MqGatewayInputOutputProvider {
    return MqGatewayInputOutputProvider(MqGatewayPlatformConfigurationFactory().create(platformConfiguration))
  }

  override fun hardwareConnectorFactory(): MqGatewayConnectorFactory {
    return MqGatewayConnectorFactory()
  }

  override fun connectorClass(): KClass<MqGatewayConnector> {
    return MqGatewayConnector::class
  }
}