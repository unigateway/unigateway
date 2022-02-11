package com.mqgateway.core.hardware.io

interface BinaryOutput {
  fun setState(state: BinaryState)
}

interface BinaryInput {
  fun addListener(listener: BinaryStateListener)
  fun getState(): BinaryState
}

enum class BinaryState {
  HIGH, LOW;

  fun inverse(): BinaryState {
    return if (this == HIGH) LOW else HIGH
  }
}

interface BinaryStateChangeEvent {
  fun getState(): BinaryState
}

fun interface BinaryStateListener {
  fun handle(event: BinaryStateChangeEvent)
}
