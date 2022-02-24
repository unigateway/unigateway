package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener

class MqGatewayDigitalPinInput : BinaryInput {
  override fun addListener(listener: BinaryStateListener) {
    TODO("Not yet implemented")
  }

  override fun getState(): BinaryState {
    TODO("Not yet implemented")
  }
}

class MqGatewayDigitalPinOutput : BinaryOutput {
  override fun setState(newState: BinaryState) {
    TODO("Not yet implemented")
  }
}
