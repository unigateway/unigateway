package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

const val DEFAULT_DEBOUNCE: Int = 100;

class RaspberryPiConnectorFactory : HardwareConnectorFactory<RaspberryPiConnector> {

  override fun create(config: Map<String, *>): RaspberryPiConnector {
    val debounce = config["debounce"] as Int? // todo default used in RaspberryPiInputOutputProvider
    return RaspberryPiConnector(config["pin"] as Int, debounce ?: DEFAULT_DEBOUNCE)
  }
}
