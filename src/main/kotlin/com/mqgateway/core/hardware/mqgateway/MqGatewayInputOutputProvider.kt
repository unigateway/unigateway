package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.hardware.mqgateway.mcp.MqGatewayMcpExpanders
import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class MqGatewayInputOutputProvider(
  private val mcpExpanders: MqGatewayMcpExpanders,
  private val platformConfiguration: MqGatewayPlatformConfiguration
) : HardwareInputOutputProvider<MqGatewayConnector> {

  override fun getBinaryInput(connector: MqGatewayConnector): BinaryInput {
    return mcpExpanders.getInputPin(connector.portNumber, connector.wireColor, connector.debounceMs ?: platformConfiguration.defaultDebounceMs)
  }

  override fun getBinaryOutput(connector: MqGatewayConnector): BinaryOutput {
    return mcpExpanders.getOutputPin(connector.portNumber, connector.wireColor)
  }

  override fun getFloatInput(connector: MqGatewayConnector): FloatInput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }

  override fun getFloatOutput(connector: MqGatewayConnector): FloatOutput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }
}

class UnsupportedMqGatewayConnectorException(message: String) : RuntimeException(message)
