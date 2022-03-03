package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

class RaspberryPiConnectorFactory : HardwareConnectorFactory<RaspberryPiConnector> {

  override fun create(config: Map<String, *>): RaspberryPiConnector {
    return RaspberryPiConnector(config["pin"] as Int, config["debounce"] as Int?)
  }
}
