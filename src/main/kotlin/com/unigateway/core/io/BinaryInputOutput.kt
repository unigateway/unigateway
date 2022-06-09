package com.unigateway.core.io

/**
 * Simple output which can be set with one of two states: HIGH, LOW
 * Depending on implementation, may represent hardware digital pin, process communication, external communication, etc.
 */
interface BinaryOutput {
  fun setState(newState: BinaryState)
}

/**
 * Simple input which can receive one of two states: HIGH, LOW
 * Depending on implementation, may represent hardware digital pin, process communication, external communication, etc.
 */
interface BinaryInput {
  fun addListener(listener: BinaryStateListener)
  fun getState(): BinaryState
}

enum class BinaryState {
  HIGH, LOW;

  /**
   * @return inverted state, i.e. HIGH to LOW, LOW to HIGH
   */
  fun invert(): BinaryState {
    return if (this == HIGH) LOW else HIGH
  }
}

/**
 * Event on change of BinaryOutput
 * @see BinaryOutput
 */
interface BinaryStateChangeEvent {
  fun newState(): BinaryState
}

fun interface BinaryStateListener {
  fun handle(event: BinaryStateChangeEvent)
}
