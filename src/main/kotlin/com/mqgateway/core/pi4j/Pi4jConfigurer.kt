package com.mqgateway.core.pi4j

import com.mqgateway.configuration.GatewaySystemProperties
import com.pi4j.platform.Platform
import com.pi4j.platform.PlatformManager

object Pi4jConfigurer {

  fun setup(systemPlatform: GatewaySystemProperties.SystemPlatform) {
    PlatformManager.setPlatform(Platform.valueOf(systemPlatform.name))
  }
}
