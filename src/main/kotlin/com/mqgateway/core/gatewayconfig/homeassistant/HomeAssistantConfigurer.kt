package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.configuration.HomeAssistantProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantConfigurer(
  private val properties: HomeAssistantProperties,
  private val converter: HomeAssistantConverter,
  private val publisher: HomeAssistantPublisher,
  private val mqttClientFactory: MqttClientFactory
) {

  fun sendHomeAssistantConfiguration(gatewayConfiguration: GatewayConfiguration) {
    LOGGER.info { "Publishing HomeAssistant configuration" }
    val mqttClient = mqttClientFactory.create(
      "${gatewayConfiguration.name}-homeassistant-configurator",
      { LOGGER.info { "HomeAssistant configurator connected" } },
      { LOGGER.info { "HomeAssistant configurator disconnected" } }
    )

    mqttClient.connect(MqttMessage("${properties.rootTopic}/state", "disconnected", 0, false), true)

    val components = converter.convert(gatewayConfiguration)
    publisher.cleanPublishedConfigurations(mqttClient, properties.rootTopic, gatewayConfiguration.name)
    publisher.publish(mqttClient, properties.rootTopic, components)

    mqttClient.disconnect()
    LOGGER.info { "HomeAssistant configuration published" }
  }
}
