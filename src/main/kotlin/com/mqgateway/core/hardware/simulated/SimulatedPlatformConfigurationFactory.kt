package com.mqgateway.core.hardware.simulated

class SimulatedPlatformConfigurationFactory {
  fun create(configMap: Map<String, *>): SimulatedPlatformConfiguration {
    val someConfig = configMap[SOME_CONFIG_KEY] as String? ?: "default"
    return SimulatedPlatformConfiguration(someConfig)
  }

  companion object {
    private const val SOME_CONFIG_KEY = "some-config"
  }
}
