package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

class MqGatewayConnectorFactory : HardwareConnectorFactory<MqGatewayConnector> {

  override fun create(config: Map<String, *>): MqGatewayConnector {
    // TODO need some validation to be implemented with MqGateway Hardware Interface
    return MqGatewayConnector(config["portNumber"] as Int, WireColor.valueOf(config["wireColor"] as String))
  }
}
