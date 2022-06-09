package com.unigateway.core.hardware.simulated

import com.unigateway.core.gatewayconfig.connector.HardwareConnectorFactory

class SimulatedConnectorFactory : HardwareConnectorFactory<SimulatedConnector> {

  override fun create(config: Map<String, *>): SimulatedConnector {
    return SimulatedConnector(config["pin"] as Int, config["initialValue"] as String?)
  }
}
