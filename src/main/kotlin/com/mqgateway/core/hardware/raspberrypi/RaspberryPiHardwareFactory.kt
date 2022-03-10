package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.configuration.HardwareInterfaceFactory
import kotlin.reflect.KClass

class RaspberryPiHardwareFactory : HardwareInterfaceFactory<RaspberryPiConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>): RaspberryPiInputOutputProvider {
    return RaspberryPiInputOutputProvider(RaspberryPiPlatformConfiguration())
  }

  override fun hardwareConnectorFactory(): RaspberryPiConnectorFactory {
    return RaspberryPiConnectorFactory()
  }

  override fun connectorClass(): KClass<RaspberryPiConnector> {
    return RaspberryPiConnector::class
  }
}
