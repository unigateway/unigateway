package com.unigateway.core.hardware.raspberrypi

import com.unigateway.configuration.HardwareInterfaceFactory
import com.unigateway.core.gatewayconfig.validation.GatewayValidator
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
