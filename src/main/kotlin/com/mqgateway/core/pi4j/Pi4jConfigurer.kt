package com.mqgateway.core.pi4j

import com.pi4j.platform.Platform
import com.pi4j.platform.PlatformManager
import com.mqgateway.core.gatewayconfig.SystemPlatform

object Pi4jConfigurer {

  fun setup(systemPlatform: SystemPlatform) {
    PlatformManager.setPlatform(Platform.valueOf(systemPlatform.name))
  }
}