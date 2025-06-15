package com.mqgateway.core.utils

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import java.time.LocalDateTime

@Singleton
class TimersScheduler {
  private val timers: MutableSet<SchedulableTimer> = mutableSetOf()

  fun registerTimer(timer: SchedulableTimer) {
    timers.add(timer)
  }

  fun unregisterTimer(timer: SchedulableTimer) {
    timers.remove(timer)
  }

  @Scheduled(fixedDelay = "30s")
  fun checkTimers() {
    timers.forEach { it.updateTimer(LocalDateTime.now()) }
  }

  interface SchedulableTimer {
    fun updateTimer(dateTime: LocalDateTime)
  }
}
