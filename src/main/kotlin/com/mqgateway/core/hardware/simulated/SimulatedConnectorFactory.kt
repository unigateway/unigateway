package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

class SimulatedConnectorFactory : HardwareConnectorFactory<SimulatedConnector> {

  override fun create(config: Map<String, *>): SimulatedConnector {
    return SimulatedConnector(config["pinNumber"] as Int)
  }
}
