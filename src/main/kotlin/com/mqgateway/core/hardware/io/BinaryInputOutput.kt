package com.mqgateway.core.hardware.io

interface BinaryOutput {
  fun setState(state: BinaryState)
}

interface BinaryInput {
  fun addListener(listener: BinaryStateListener)
  fun getState(): BinaryState
}

enum class BinaryState {
  HIGH, LOW
}

interface BinaryStateChangeEvent {
  fun getState(): BinaryState
}

fun interface BinaryStateListener {
  fun handleBinaryStateChangeEvent(event: BinaryStateChangeEvent)
}
