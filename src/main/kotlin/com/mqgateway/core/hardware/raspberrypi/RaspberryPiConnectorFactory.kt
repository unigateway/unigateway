package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

const val DEFAULT_DEBOUNCE: Int = 50

class RaspberryPiConnectorFactory : HardwareConnectorFactory<RaspberryPiConnector> {

  override fun create(config: Map<String, *>): RaspberryPiConnector {
    val debounce = config["debounce"] as Int?
    return RaspberryPiConnector(config["pin"] as Int, debounce ?: DEFAULT_DEBOUNCE)
  }
}
