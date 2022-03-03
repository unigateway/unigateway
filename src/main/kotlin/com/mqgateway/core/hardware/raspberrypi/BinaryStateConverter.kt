package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.BinaryState

object BinaryStateConverter {
  fun fromBoolean(value: Boolean): BinaryState {
    return if (value) BinaryState.HIGH else BinaryState.LOW
  }

  fun toBoolean(binaryState: BinaryState): Boolean {
    return binaryState == BinaryState.HIGH
  }
}
