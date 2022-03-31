package com.mqgateway.core.hardware.raspberrypi

data class RaspberryPiPlatformConfiguration(
  val defaultDebounceMs: Int = 50,
  val defaultPullUpDown: PullUpDown = PullUpDown.PULL_UP
)
