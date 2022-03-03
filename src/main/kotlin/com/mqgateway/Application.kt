package com.mqgateway

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.UpdateListener
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.homie.HomieDevice
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.Micronaut.build
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
  build()
    .args(*args)
    .packages("com.mqgateway")
    .start()
}

@Singleton
class MqGateway(
  private val deviceRegistry: DeviceRegistry,
  private val systemInfoProvider: SystemInfoProvider,
  private val homieDevice: HomieDevice,
  private val updateListeners: List<UpdateListener>
) {

  @EventListener
  fun initialize(event: StartupEvent) {
    LOGGER.info { "MqGateway started. Initialization..." }

    LOGGER.info { systemInfoProvider.getSummary() }

    updateListeners.forEach { deviceRegistry.addUpdateListener(it) }
    homieDevice.connect()
    deviceRegistry.initializeDevices()

    LOGGER.info { "Initialization finished successfully. Running normally." }
  }

  @EventListener
  fun close(event: ShutdownEvent) {
    LOGGER.info { "Closing MqGateway..." }
    homieDevice.disconnect()
    LOGGER.info { "MqGateway closed" }
  }
}
