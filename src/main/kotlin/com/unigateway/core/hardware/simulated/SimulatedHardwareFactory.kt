package com.unigateway.core.hardware.simulated

import com.unigateway.configuration.HardwareInterfaceFactory
import kotlin.reflect.KClass

class SimulatedHardwareFactory(private val platformConfiguration: Map<String, *>) : HardwareInterfaceFactory<SimulatedConnector> {

  override fun hardwareInputOutputProvider(): SimulatedInputOutputProvider {
    return SimulatedInputOutputProvider(SimulatedPlatformConfigurationFactory().create(platformConfiguration))
  }

  override fun hardwareConnectorFactory(): SimulatedConnectorFactory {
    return SimulatedConnectorFactory()
  }

  override fun connectorClass(): KClass<SimulatedConnector> {
    return SimulatedConnector::class
  }
}
