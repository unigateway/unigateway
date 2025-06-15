package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatValueChangeEvent
import com.mqgateway.core.io.FloatValueListener

class SimulatedFloatInput(initialValue: Float) : FloatInput {
  private var value: Float = initialValue
  private val listeners: MutableList<FloatValueListener> = mutableListOf()

  override fun addListener(listener: FloatValueListener) {
    listeners.add(listener)
  }

  override fun getValue() = value

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

  fun getValue(): Float = value ?: throw IllegalStateException("Should never ask for the value if it was not set before")
}

data class SimulatedFloatValueChangeEvent(val newValue: Float) : FloatValueChangeEvent {
  override fun newValue() = newValue
}
