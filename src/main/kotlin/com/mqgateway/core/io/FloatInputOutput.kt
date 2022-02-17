package com.mqgateway.core.io

/**
 * Numerical output which can be set with float value
 * Depending on implementation, may represent hardware analog pin, process communication, external communication, etc.
 */
interface FloatInput {
  fun addListener(listener: FloatStateListener)
  fun getValue(): Float
}

/**
 * Numerical output which can receive float value
 * Depending on implementation, may represent hardware analog pin, process communication, external communication, etc.
 */
interface FloatOutput {
  fun setValue(state: Float)
}

/**
 * Event on change of FloatOutput
 * @see BinaryOutput
 */
interface FloatStateChangeEvent {
  fun newValue(): Float
}

fun interface FloatStateListener {
  fun handle(event: FloatStateChangeEvent)
}
