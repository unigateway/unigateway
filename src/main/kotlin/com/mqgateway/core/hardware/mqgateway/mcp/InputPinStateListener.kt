package com.mqgateway.core.hardware.mqgateway.mcp

import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant

private val LOGGER = KotlinLogging.logger {}

class InputPinStateListener(private val debounceMs: Long, initialState: BinaryState, private val eventListener: BinaryStateListener) {
  init {
    require(debounceMs >= 0) { "Debounce must be non-negative, was $debounceMs" }
  }

  var knownState: BinaryState = initialState
  var ongoingChangeTime: Instant? = null

  fun handle(
    newState: BinaryState,
    currentTime: Instant,
  ) {
    if (debouncePinState(newState, currentTime) != knownState) {
      LOGGER.debug { "State changed from $knownState to $newState (debounced: $debounceMs)" }
      eventListener.handle(MqGatewayExpanderPinStateChangeEvent(knownState, newState))
      knownState = newState
    }
  }

  private fun debouncePinState(
    newState: BinaryState,
    currentTime: Instant,
  ): BinaryState {
    if (knownState != newState) {
      if (ongoingChangeTime == null && debounceMs > 0) {
        ongoingChangeTime = currentTime
        return knownState
      }
      if (debounceMs == 0L || !currentTime.isBefore(ongoingChangeTime!!.plusMillis(debounceMs))) {
        ongoingChangeTime = null
        return newState
      }
      return knownState
    }
    ongoingChangeTime = null
    return knownState
  }
}
