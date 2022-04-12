package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.configuration.HardwareInterfaceFactory
import com.mqgateway.core.hardware.mqgateway.mcp.MqGatewayMcpExpanders
import kotlin.reflect.KClass

class MqGatewayHardwareFactory : HardwareInterfaceFactory<MqGatewayConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>): MqGatewayInputOutputProvider {
    val configuration = MqGatewayPlatformConfigurationFactory().create(platformConfiguration)
    val mcpExpanders = MqGatewayMcpExpanders(configuration.components.mcp23017.getPorts())
    mcpExpanders.start()
    return MqGatewayInputOutputProvider(mcpExpanders, configuration)
  }

  override fun hardwareConnectorFactory(): MqGatewayConnectorFactory {
    return MqGatewayConnectorFactory()
  }

  override fun connectorClass(): KClass<MqGatewayConnector> {
    return MqGatewayConnector::class
  }
}
