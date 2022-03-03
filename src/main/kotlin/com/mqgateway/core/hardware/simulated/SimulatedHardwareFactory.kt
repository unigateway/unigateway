package com.mqgateway.core.hardware.simulated

import com.mqgateway.configuration.HardwareInterfaceFactory

class SimulatedHardwareFactory: HardwareInterfaceFactory<SimulatedConnector> {

  override fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>?): SimulatedInputOutputProvider {
    return SimulatedInputOutputProvider(SimulatedPlatformConfigurationFactory().create(platformConfiguration ?: emptyMap<String, Any>()))
  }

  override fun hardwareConnectorFactory(): SimulatedConnectorFactory {
    return SimulatedConnectorFactory()
  }
}
