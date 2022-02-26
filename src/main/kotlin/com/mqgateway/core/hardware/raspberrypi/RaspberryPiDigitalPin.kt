package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener

class RaspberryPiDigitalPinInput : BinaryInput {
  override fun addListener(listener: BinaryStateListener) {
    TODO("Not yet implemented")
  }

  override fun getState(): BinaryState {
    TODO("Not yet implemented")
  }
}

class RaspberryPiDigitalPinOutput : BinaryOutput {
  override fun setState(newState: BinaryState) {
    TODO("Not yet implemented")
  }
}
