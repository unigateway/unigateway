package com.mqgateway

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.GatewayHomieUpdateListener
import io.micronaut.runtime.Micronaut.build
import mu.KotlinLogging
import javax.inject.Singleton

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
  val context = build()
    .args(*args)
    .packages("com.mqgateway")
    .start()

  val mqGateway = context.getBean(MqGateway::class.java)
  mqGateway.initialize()
}

@Singleton
class MqGateway(
  private val deviceRegistry: DeviceRegistry,
  private val homieDevice: HomieDevice,
  private val homieReceiver: GatewayHomieReceiver
) {

  fun initialize() {
    LOGGER.info { "MqGateway started. Initialization..." }

    homieDevice.connect(homieReceiver)
    deviceRegistry.addUpdateListener(GatewayHomieUpdateListener(homieDevice))
    deviceRegistry.initializeDevices()

    LOGGER.info { "Initialization finished successfully. Running normally." }
  }
}
