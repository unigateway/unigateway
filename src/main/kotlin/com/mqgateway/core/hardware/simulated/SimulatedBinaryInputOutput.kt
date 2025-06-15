package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateChangeEvent
import com.mqgateway.core.io.BinaryStateListener

class SimulatedBinaryInput(initialState: BinaryState) : BinaryInput {
  private var state: BinaryState = initialState
  private val listeners: MutableList<BinaryStateListener> = mutableListOf()

  override fun addListener(listener: BinaryStateListener) {
    listeners.add(listener)
  }

  override fun getState(): BinaryState = state

  fun setState(newState: BinaryState) {
    if (state != newState) {
      state = newState
      listeners.forEach { it.handle(SimulatedBinaryStateChangeEvent(newState)) }
    }
  }

  fun high() {
    setState(BinaryState.HIGH)
  }

  fun low() {
    setState(BinaryState.LOW)
  }
}

class SimulatedBinaryOutput : BinaryOutput {
  private var state: BinaryState? = null

  override fun setState(newState: BinaryState) {
    state = newState
  }

  fun getState(): BinaryState = state ?: throw IllegalStateException("Should never ask for the value if it was not set before")
}

data class SimulatedBinaryStateChangeEvent(val newState: BinaryState) : BinaryStateChangeEvent {
  override fun newState() = newState
}
