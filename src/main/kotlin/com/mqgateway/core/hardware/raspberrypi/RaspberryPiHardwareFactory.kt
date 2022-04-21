package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.configuration.HardwareInterfaceFactory
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import kotlin.reflect.KClass

class RaspberryPiHardwareFactory : HardwareInterfaceFactory<RaspberryPiConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>): RaspberryPiInputOutputProvider {
    val configuration = RaspberryPiPlatformConfiguration.create(platformConfiguration)
    return RaspberryPiInputOutputProvider(configuration)
  }

  override fun hardwareConnectorFactory(): RaspberryPiConnectorFactory {
    return RaspberryPiConnectorFactory()
  }

  override fun connectorClass(): KClass<RaspberryPiConnector> {
    return RaspberryPiConnector::class
  }

  override fun configurationValidator(): GatewayValidator {
    return RaspberryPiGatewayValidator()
  }
}
