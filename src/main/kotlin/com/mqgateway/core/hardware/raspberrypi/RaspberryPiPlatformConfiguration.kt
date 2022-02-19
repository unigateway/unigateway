package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.configuration.GatewaySystemProperties

data class RaspberryPiPlatformConfiguration(
  val defaultDebounceMs: Int = 0
) : GatewaySystemProperties.PlatformConfiguration
