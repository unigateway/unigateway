package com.mqgateway

import com.mqgateway.configuration.HomeAssistantProperties
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantConfigurer
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.GatewayHomieUpdateListener
import io.micronaut.runtime.Micronaut.build
import mu.KotlinLogging
import javax.inject.Inject
import javax.inject.Singleton

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
  val context = build()
    .args(*args)
    .packages("com.mqgateway")
    .start()

  try {
    val mqGateway = context.getBean(MqGateway::class.java)
    mqGateway.initialize()
  } catch (throwable: Throwable) {
    LOGGER.error(throwable) { "Fatal error" }
    throw throwable
  }
}

@Singleton
class MqGateway(
  private val deviceRegistry: DeviceRegistry,
  private val homieDevice: HomieDevice,
  private val homieReceiver: GatewayHomieReceiver,
  private val gateway: Gateway,
  private val gatewayConfigLoader: ConfigLoader,
  private val homeAssistantProperties: HomeAssistantProperties
) {

  @Inject
  lateinit var homeAssistantConfigurer: HomeAssistantConfigurer

  fun initialize() {
    LOGGER.info { "MqGateway started. Initialization..." }

    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
      LOGGER.error(exception) { "Uncaught exception in thread '${thread.name}'." }
    }

    homieDevice.connect(homieReceiver)
    deviceRegistry.addUpdateListener(GatewayHomieUpdateListener(homieDevice))
    deviceRegistry.initializeDevices()

    if (homeAssistantProperties.enabled && gatewayConfigLoader.configReloaded) {
      homeAssistantConfigurer.sendHomeAssistantConfiguration(gateway)
    }

    LOGGER.info { "Initialization finished successfully. Running normally." }
  }
}
