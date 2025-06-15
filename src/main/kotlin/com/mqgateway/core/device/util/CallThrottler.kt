package com.mqgateway.core.device.util

import java.time.Clock
import java.time.Instant

class CallThrottler(private val minIntervalMillis: Long) {
  private var clock = Clock.systemDefaultZone()
  private var lastUpdateTimestamp: Instant = Instant.MIN

  fun throttle(throttledFun: () -> Unit) {
    if (minIntervalMillis <= 0) {
      throttledFun()
      return
    }
    if (clock.instant().minusMillis(minIntervalMillis).isAfter(lastUpdateTimestamp)) {
      lastUpdateTimestamp = clock.instant()
      throttledFun()
      return
    }
  }

  fun setClockForTests(clock: Clock) {
    this.clock = clock
  }
}
