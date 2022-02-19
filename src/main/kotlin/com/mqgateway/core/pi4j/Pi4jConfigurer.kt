package com.mqgateway.core.pi4j

import com.pi4j.platform.Platform
import com.pi4j.platform.PlatformManager

object Pi4jConfigurer {

  fun setup() {
    PlatformManager.setPlatform(Platform.valueOf("NANOPI")) // TODO needs to be removed
  }
}
