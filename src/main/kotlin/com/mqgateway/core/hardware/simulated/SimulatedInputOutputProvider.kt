package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateChangeEvent
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatValueChangeEvent
import com.mqgateway.core.io.FloatValueListener
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class SimulatedInputOutputProvider : HardwareInputOutputProvider<SimulatedConnector> {

  override fun getBinaryInput(connector: SimulatedConnector): SimulatedBinaryInput {
    return SimulatedBinaryInput()
  }

  override fun getBinaryOutput(connector: SimulatedConnector): SimulatedBinaryOutput {
    return SimulatedBinaryOutput()
  }

  override fun getFloatInput(connector: SimulatedConnector): SimulatedFloatInput {
    return SimulatedFloatInput()
  }

  override fun getFloatOutput(connector: SimulatedConnector): SimulatedFloatOutput {
    return SimulatedFloatOutput()
  }
}

data class SimulatedConnector(
  val pinNumber: Int
) : HardwareConnector

class SimulatedBinaryInput(initialState: BinaryState? = null) : BinaryInput {

  private var state: BinaryState? = initialState
  private val listeners: MutableList<BinaryStateListener> = mutableListOf()

  override fun addListener(listener: BinaryStateListener) {
    listeners.add(listener)
  }

  override fun getState(): BinaryState = state ?: throw IllegalStateException("Should never ask for the state if it was not set before")

  fun setState(newState: BinaryState) {
    if (state != newState) {
      listeners.forEach { it.handle(SimulatedBinaryStateChangeEvent(newState)) }
    }
  }
}

data class SimulatedBinaryStateChangeEvent(val newState: BinaryState) : BinaryStateChangeEvent {
  override fun newState() = newState
}

class SimulatedBinaryOutput : BinaryOutput {

  private var state: BinaryState? = null

  override fun setState(newState: BinaryState) {
    state = newState
  }

  fun getState() = state
}

class SimulatedFloatInput(initialValue: Float? = null) : FloatInput {

  private var value: Float? = initialValue
  private val listeners: MutableList<FloatValueListener> = mutableListOf()

  override fun addListener(listener: FloatValueListener) {
    listeners.add(listener)
  }

  override fun getValue() = value ?: throw IllegalStateException("Should never ask for the value if it was not set before")

  fun setValue(newValue: Float) {
    if (value != newValue) {
      listeners.forEach { it.handle(SimulatedFloatValueChangeEvent(newValue)) }
    }
  }
}

class SimulatedFloatOutput : FloatOutput {

  private var value: Float? = null

  override fun setValue(newValue: Float) {
    value = newValue
  }

  fun getValue() = value
}

data class SimulatedFloatValueChangeEvent(val newValue: Float) : FloatValueChangeEvent {
  override fun newValue() = newValue
}
