package com.mqgateway.core.utils

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import javax.inject.Singleton

@Singleton
@Requires(beans = [SerialConnection::class])
class SerialConnectionDataFetchScheduler(private val serialConnection: SerialConnection) {

  @Scheduled(fixedDelay = "30s")
  fun getDataForAllListeners() {
    serialConnection.getDataForAllListeners()
  }
}
