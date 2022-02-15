package com.mqgateway.core.io

/**
 * Numerical output which can be set with float value
 * Depending on implementation, may represent hardware analog pin, process communication, external communication, etc.
 */
interface FloatInput {
  fun addListener(listener: FloatStateListener)
  fun getState(): Float
}

/**
 * Numerical output which can receive float value
 * Depending on implementation, may represent hardware analog pin, process communication, external communication, etc.
 */
interface FloatOutput {
  fun setState(state: Float)
}

/**
 * Event on change of FloatOutput
 * @see BinaryOutput
 */
interface FloatStateChangeEvent {
  fun newState(): Float
}

fun interface FloatStateListener {
  fun handle(event: FloatStateChangeEvent)
}
