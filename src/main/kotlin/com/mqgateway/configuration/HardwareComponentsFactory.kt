package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.mqgateway.core.hardware.mqgateway.MqGatewayHardwareFactory
import com.mqgateway.core.hardware.raspberrypi.RaspberryPiHardwareFactory
import com.mqgateway.core.hardware.simulated.SimulatedHardwareFactory
import com.mqgateway.core.io.provider.HardwareInputOutputProvider
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class HardwareComponentsFactory {

  // TODO add information to documentation on how to implement new HardwareInterface
  @Singleton
  fun hardwareInterfaceFactory(gatewaySystemProperties: GatewaySystemProperties): HardwareInterfaceFactory<*> {
    return when (gatewaySystemProperties.platform) {
      "SIMULATED" -> SimulatedHardwareFactory()
      "MQGATEWAY" -> MqGatewayHardwareFactory()
      "RASPBERRYPI" -> RaspberryPiHardwareFactory()
      else -> throw IllegalStateException()
    }
  }

  @Singleton
  fun hardwareInputOutputProvider(hardwareInterfaceFactory: HardwareInterfaceFactory<*>, gatewaySystemProperties: GatewaySystemProperties): HardwareInputOutputProvider<*> {
    return hardwareInterfaceFactory.hardwareInputOutputProvider(gatewaySystemProperties.platformConfig)
  }

  @Singleton
  fun hardwareConnectorFactory(hardwareInterfaceFactory: HardwareInterfaceFactory<*>): HardwareConnectorFactory<*> {
    return hardwareInterfaceFactory.hardwareConnectorFactory()
  }
}
