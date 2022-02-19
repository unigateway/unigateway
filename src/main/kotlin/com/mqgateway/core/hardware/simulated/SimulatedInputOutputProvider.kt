package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatStateListener
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class SimulatedInputOutputProvider : HardwareInputOutputProvider<SimulatedConnector> {

  override fun getBinaryInput(connector: SimulatedConnector): SimulatedBinaryInput {
    TODO("Not yet implemented")
  }

  override fun getBinaryOutput(connector: SimulatedConnector): SimulatedBinaryOutput {
    TODO("Not yet implemented")
  }

  override fun getFloatInput(connector: SimulatedConnector): SimulatedFloatInput {
    TODO("Not yet implemented")
  }

  override fun getFloatOutput(connector: SimulatedConnector): SimulatedFloatOutput {
    TODO("Not yet implemented")
  }
}

data class SimulatedConnector(
  val pinNumber: Int
) : HardwareConnector()

class SimulatedBinaryInput : BinaryInput {
  override fun addListener(listener: BinaryStateListener) {
    TODO("Not yet implemented")
  }

  override fun getState(): BinaryState {
    TODO("Not yet implemented")
  }
}

class SimulatedBinaryOutput : BinaryOutput {
  override fun setState(state: BinaryState) {
    TODO("Not yet implemented")
  }
}

class SimulatedFloatInput : FloatInput {

  override fun addListener(listener: FloatStateListener) {
    TODO("Not yet implemented")
  }

  override fun getValue(): Float {
    TODO("Not yet implemented")
  }
}

class SimulatedFloatOutput : FloatOutput {
  override fun setValue(state: Float) {
    TODO("Not yet implemented")
  }
}
