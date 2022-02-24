package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.provider.HardwareConnector

data class RaspberryPiConnector(
  val pinNumber: Int,
  val debounceMs: Int = 0
) : HardwareConnector
