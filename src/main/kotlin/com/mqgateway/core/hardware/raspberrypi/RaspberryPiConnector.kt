package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.provider.HardwareConnector
import kotlinx.serialization.Serializable

@Serializable
data class RaspberryPiConnector(
  val pin: Int,
  val debounceMs: Int?
) : HardwareConnector
