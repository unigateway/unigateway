package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.configuration.HomeAssistantProperties
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.GatewayConfigChangedListener
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantConfigurer(
  private val properties: HomeAssistantProperties,
  private val converter: HomeAssistantConverter,
  private val publisher: HomeAssistantPublisher,
  private val mqttClientFactory: MqttClientFactory
) : GatewayConfigChangedListener {

  private fun sendHomeAssistantConfiguration(gateway: Gateway) {
    val mqttClient = mqttClientFactory.create(
      "${gateway.name}-homeassistant-configurator",
      { LOGGER.info { "HomeAssistant configurator connected" } },
      { LOGGER.error { "HomeAssistant configurator disconnected unexpectedly" } })

    mqttClient.connect(MqttMessage("${properties.rootTopic}/state", "disconnected", 0, false), true)

    val components = converter.convert(gateway)
    publisher.publish(mqttClient, properties.rootTopic, components)

    mqttClient.disconnect()
  }

  override fun onGatewayConfigChanged(gateway: Gateway) {
    LOGGER.info { "Publishing HomeAssistant configuration" }
    sendHomeAssistantConfiguration(gateway)
    LOGGER.info { "HomeAssistant configuration published" }
  }
}
