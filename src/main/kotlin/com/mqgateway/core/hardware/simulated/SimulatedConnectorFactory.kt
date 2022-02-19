package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

class SimulatedConnectorFactory : HardwareConnectorFactory<SimulatedConnector> {

  override fun create(config: HashMap<String, Any>): SimulatedConnector {
    TODO("Not yet implemented")
  }
}
