package com.mqgateway

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.PropertyInitializer
import com.mqgateway.core.device.UpdateListener
import com.mqgateway.core.device.UpdateListenersInitializedEvent
import com.mqgateway.core.utils.SystemInfoProvider
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.Micronaut.build
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import mu.KotlinLogging
import java.util.EventObject

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
  private val propertyInitializers: List<PropertyInitializer>,
  private val updateListeners: List<UpdateListener>,
  private val eventPublisher: ApplicationEventPublisher<EventObject>
) {

  @EventListener
  fun initialize(@Suppress("UNUSED_PARAMETER") event: StartupEvent) {
    LOGGER.info { "MqGateway started. Initialization..." }

    LOGGER.info { systemInfoProvider.getSummary() }

    updateListeners.forEach { deviceRegistry.addUpdateListener(it) }
    eventPublisher.publishEvent(UpdateListenersInitializedEvent())

    propertyInitializers.forEach { it.initializeValues() }
    deviceRegistry.initializeDevices()

    LOGGER.info { "Initialization finished successfully. Running normally." }
  }

  @EventListener
  fun close(@Suppress("UNUSED_PARAMETER") event: ShutdownEvent) {
    LOGGER.info { "Closing MqGateway" }
  }
}
