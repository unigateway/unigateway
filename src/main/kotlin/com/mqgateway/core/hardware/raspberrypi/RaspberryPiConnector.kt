package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.provider.HardwareConnector
import kotlinx.serialization.Serializable

@Serializable
data class RaspberryPiConnector
  @JvmOverloads
  constructor(
    val gpio: Int,
    val debounceMs: Int? = null,
    val pullUpDown: PullUpDown? = null,
  ) : HardwareConnector

enum class PullUpDown {
  PULL_UP,
  PULL_DOWN,
}
