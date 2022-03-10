package com.mqgateway.core.hardware.simulated

import com.mqgateway.configuration.HardwareInterfaceFactory
import kotlin.reflect.KClass

class SimulatedHardwareFactory : HardwareInterfaceFactory<SimulatedConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>): SimulatedInputOutputProvider {
    return SimulatedInputOutputProvider(SimulatedPlatformConfigurationFactory().create(platformConfiguration))
  }

  override fun hardwareConnectorFactory(): SimulatedConnectorFactory {
    return SimulatedConnectorFactory()
  }

  override fun connectorClass(): KClass<SimulatedConnector> {
    return SimulatedConnector::class
  }
}
