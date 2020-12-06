package com.mqgateway.core.utils

import io.micronaut.scheduling.annotation.Scheduled
import javax.inject.Singleton

@Singleton
class SerialConnectionDataFetchScheduler(private val serialConnection: SerialConnection) {

  @Scheduled(fixedDelay = "30s")
  fun getDataForAllListeners() {
    serialConnection.getDataForAllListeners()
  }
}
