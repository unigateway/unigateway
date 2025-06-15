package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.configuration.HardwareInterfaceFactory
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import kotlin.reflect.KClass

class RaspberryPiHardwareFactory(platformConfiguration: Map<String, *>) : HardwareInterfaceFactory<RaspberryPiConnector> {
  private val platformConfiguration = RaspberryPiPlatformConfiguration.create(platformConfiguration)

  override fun hardwareInputOutputProvider(): RaspberryPiInputOutputProvider {
    return RaspberryPiInputOutputProvider(platformConfiguration)
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
