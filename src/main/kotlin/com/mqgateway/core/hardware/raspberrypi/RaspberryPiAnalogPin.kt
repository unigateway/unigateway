package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatValueListener

class RaspberryPiAnalogPinInput : FloatInput {

  override fun addListener(listener: FloatValueListener) {
    TODO("Not yet implemented")
  }

  override fun getValue(): Float {
    TODO("Not yet implemented")
  }
}

class RaspberryPiAnalogPinOutput : FloatOutput {
  override fun setValue(newValue: Float) {
    TODO("Not yet implemented")
  }
}
