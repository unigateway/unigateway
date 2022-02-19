package com.mqgateway.core.hardware.simulated

import com.mqgateway.configuration.GatewaySystemProperties

data class SimulatedPlatformConfiguration(
  val defaultDebounceMs: Int = 0
) : GatewaySystemProperties.PlatformConfiguration
