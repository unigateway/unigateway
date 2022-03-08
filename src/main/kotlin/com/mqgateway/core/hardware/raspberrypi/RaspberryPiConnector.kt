package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.provider.HardwareConnector

data class RaspberryPiConnector(
  val pin: Int,
  val debounceMs: Int?
) : HardwareConnector
