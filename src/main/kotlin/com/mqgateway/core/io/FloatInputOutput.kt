package com.mqgateway.core.io

/**
 * Numerical input which can be set with float value
 * Depending on implementation, may represent hardware analog pin, process communication, external communication, etc.
 */
interface FloatInput {
  fun addListener(listener: FloatValueListener)

  fun getValue(): Float
}

/**
 * Numerical output which can receive float value
 * Depending on implementation, may represent hardware analog pin, process communication, external communication, etc.
 */
interface FloatOutput {
  fun setValue(newValue: Float)
}

/**
 * Event on change of FloatOutput
 * @see BinaryOutput
 */
interface FloatValueChangeEvent {
  fun newValue(): Float
}

fun interface FloatValueListener {
  fun handle(event: FloatValueChangeEvent)
}
