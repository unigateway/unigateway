package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.configuration.HardwareInterfaceFactory

class RaspberryPiHardwareFactory: HardwareInterfaceFactory<RaspberryPiConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>?): RaspberryPiInputOutputProvider {
    return RaspberryPiInputOutputProvider(RaspberryPiPlatformConfiguration())
  }

  override fun hardwareConnectorFactory(): RaspberryPiConnectorFactory {
    return RaspberryPiConnectorFactory()
  }
}
