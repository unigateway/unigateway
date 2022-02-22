package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatValueListener
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class RaspberryPiInputOutputProvider : HardwareInputOutputProvider<RaspberryPiConnector> {

  override fun getBinaryInput(connector: RaspberryPiConnector): RaspberryPiDigitalPinInput {
    TODO("Not yet implemented")
  }

  override fun getBinaryOutput(connector: RaspberryPiConnector): RaspberryPiDigitalPinOutput {
    TODO("Not yet implemented")
  }

  override fun getFloatInput(connector: RaspberryPiConnector): RaspberryPiAnalogPinInput {
    TODO("Not yet implemented")
  }

  override fun getFloatOutput(connector: RaspberryPiConnector): RaspberryPiAnalogPinOutput {
    TODO("Not yet implemented")
  }
}

data class RaspberryPiConnector(
  val pinNumber: Int,
  val debounceMs: Int = 0
) : HardwareConnector

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
