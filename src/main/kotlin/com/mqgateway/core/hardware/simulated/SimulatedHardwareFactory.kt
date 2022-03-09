package com.mqgateway.core.hardware.simulated

import com.mqgateway.configuration.HardwareInterfaceFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

class SimulatedHardwareFactory : HardwareInterfaceFactory<SimulatedConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>?): SimulatedInputOutputProvider {
    return SimulatedInputOutputProvider(SimulatedPlatformConfigurationFactory().create(platformConfiguration ?: emptyMap<String, Any>()))
  }

  override fun hardwareConnectorFactory(): SimulatedConnectorFactory {
    return SimulatedConnectorFactory()
  }

  override fun connectorClass(): KClass<SimulatedConnector> {
    return SimulatedConnector::class
  }

  @ExperimentalSerializationApi
  @InternalSerializationApi
  override fun connectorSerializer(): KSerializer<SimulatedConnector> {
    return SimulatedConnectorSerializer
  }
}
