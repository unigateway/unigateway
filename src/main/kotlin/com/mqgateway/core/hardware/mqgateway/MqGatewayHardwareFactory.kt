package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.configuration.HardwareInterfaceFactory

class MqGatewayHardwareFactory: HardwareInterfaceFactory<MqGatewayConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>?): MqGatewayInputOutputProvider {
    return MqGatewayInputOutputProvider(MqGatewayPlatformConfigurationFactory().create(platformConfiguration ?: emptyMap<String, Any>()))
  }

  override fun hardwareConnectorFactory(): MqGatewayConnectorFactory {
    return MqGatewayConnectorFactory()
  }
}
