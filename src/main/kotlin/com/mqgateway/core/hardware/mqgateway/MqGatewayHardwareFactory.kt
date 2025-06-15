package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.configuration.HardwareInterfaceFactory
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.hardware.mqgateway.mcp.MqGatewayMcpExpanders
import kotlin.reflect.KClass

class MqGatewayHardwareFactory(platformConfiguration: Map<String, *>) : HardwareInterfaceFactory<MqGatewayConnector> {
  private val platformConfiguration = MqGatewayPlatformConfigurationFactory().create(platformConfiguration)

  override fun hardwareInputOutputProvider(): MqGatewayInputOutputProvider {
    val mcpExpanders = MqGatewayMcpExpanders(platformConfiguration.components.mcp23017.getPorts())
    mcpExpanders.start()
    return MqGatewayInputOutputProvider(mcpExpanders, platformConfiguration)
  }

  override fun hardwareConnectorFactory(): MqGatewayConnectorFactory {
    return MqGatewayConnectorFactory()
  }

  override fun connectorClass(): KClass<MqGatewayConnector> {
    return MqGatewayConnector::class
  }

  override fun configurationValidator(): GatewayValidator {
    return MqGatewayGatewayValidator(platformConfiguration)
  }
}
