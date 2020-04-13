package com.mqgateway.core.pi4j

import com.mqgateway.core.gatewayconfig.SystemPlatform
import com.pi4j.platform.Platform
import com.pi4j.platform.PlatformManager

object Pi4jConfigurer {

  fun setup(systemPlatform: SystemPlatform) {
    PlatformManager.setPlatform(Platform.valueOf(systemPlatform.name))
  }
}
