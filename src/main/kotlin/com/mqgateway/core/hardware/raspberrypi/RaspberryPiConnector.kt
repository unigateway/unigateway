package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.provider.HardwareConnector

// todo connector does not make sense to be the same for all of the input types
//  maybe we can have nullable fields here, and throw exception in RaspberryPiInputOutputProvider or use default if necessary
data class RaspberryPiConnector(
  val pin: Int,
  val debounceMs: Int
) : HardwareConnector
