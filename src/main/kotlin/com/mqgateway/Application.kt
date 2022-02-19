package com.mqgateway

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.UpdateListener
import com.mqgateway.discovery.MulticastDnsServiceDiscovery
import com.mqgateway.homie.HomieDevice
import com.mqgateway.webapi.WebSocketLogAppender
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.Micronaut.build
import io.micronaut.runtime.event.annotation.EventListener
import mu.KotlinLogging
import javax.inject.Singleton

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
  val context = build()
    .args(*args)
    .packages("com.mqgateway")
    .start()

  // TODO do we need this ?
  Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
    LOGGER.error(exception) { "Uncaught exception in thread '${thread.name}'." }
  }
}

@Singleton
class MqGateway(
  private val deviceRegistry: DeviceRegistry,
  private val homieDevice: HomieDevice,
  private val updateListeners: List<UpdateListener>,
  private val multiCastDnsServiceDiscovery: MulticastDnsServiceDiscovery,
  private val webSocketLogAppender: WebSocketLogAppender
) {

  // TODO check what happen when exception is thrown - previously it was logging error and rethrowing exception
  @EventListener
  fun initialize(event: StartupEvent) {
    LOGGER.info { "MqGateway started. Initialization..." }

    updateListeners.forEach { deviceRegistry.addUpdateListener(it) }
    homieDevice.connect()
    deviceRegistry.initializeDevices()
    multiCastDnsServiceDiscovery.init()
    webSocketLogAppender.init()

    LOGGER.info { "Initialization finished successfully. Running normally." }
  }

  fun close() {
    LOGGER.info { "Closing MqGateway..." }
    homieDevice.disconnect()
    LOGGER.info { "MqGateway closed" }
  }
}
