package com.mqgateway.core.hardware.io

interface FloatInput {
  fun addListener(listener: FloatStateListener)
  fun getState(): BinaryState
}

interface FloatStateChangeEvent {
  fun getState(): Float
}

fun interface FloatStateListener {
  fun handleFloatStateChangeEvent(event: FloatStateChangeEvent)
}
