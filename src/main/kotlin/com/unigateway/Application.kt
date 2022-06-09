package com.unigateway

import com.unigateway.core.device.DeviceRegistry
import com.unigateway.core.device.UpdateListener
import com.unigateway.core.utils.SystemInfoProvider
import com.unigateway.homie.HomieDevice
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
    .packages("com.unigateway")
    .start()
}

@Singleton
class Unigateway(
  private val deviceRegistry: DeviceRegistry,
  private val systemInfoProvider: SystemInfoProvider,
  private val homieDevice: HomieDevice,
  private val updateListeners: List<UpdateListener>
) {

  @EventListener
  fun initialize(@Suppress("UNUSED_PARAMETER") event: StartupEvent) {
    LOGGER.info { "Unigateway started. Initialization..." }

    LOGGER.info { systemInfoProvider.getSummary() }

    updateListeners.forEach { deviceRegistry.addUpdateListener(it) }
    homieDevice.connect()
    deviceRegistry.initializeDevices()

    LOGGER.info { "Initialization finished successfully. Running normally." }
  }

  @EventListener
  fun close(@Suppress("UNUSED_PARAMETER") event: ShutdownEvent) {
    LOGGER.info { "Closing Unigateway..." }
    homieDevice.disconnect()
    LOGGER.info { "Unigateway closed" }
  }
}
