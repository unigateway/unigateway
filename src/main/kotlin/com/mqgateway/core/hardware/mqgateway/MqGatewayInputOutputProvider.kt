package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class MqGatewayInputOutputProvider : HardwareInputOutputProvider<MqGatewayConnector> {

  override fun getBinaryInput(connector: MqGatewayConnector): MqGatewayDigitalPinInput {
    TODO("Not yet implemented")
  }

  override fun getBinaryOutput(connector: MqGatewayConnector): MqGatewayDigitalPinOutput {
    TODO("Not yet implemented")
  }

  override fun getFloatInput(connector: MqGatewayConnector): FloatInput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }

  override fun getFloatOutput(connector: MqGatewayConnector): FloatOutput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }
}

class UnsupportedMqGatewayConnectorException(message: String) : RuntimeException(message)
