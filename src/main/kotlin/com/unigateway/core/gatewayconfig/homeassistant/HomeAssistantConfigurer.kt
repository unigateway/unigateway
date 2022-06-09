package com.unigateway.core.gatewayconfig.homeassistant

import com.unigateway.configuration.HomeAssistantProperties
import com.unigateway.core.device.DeviceRegistry
import com.unigateway.homie.mqtt.MqttClientFactory
import com.unigateway.homie.mqtt.MqttMessage
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantConfigurer(
  private val properties: HomeAssistantProperties,
  private val converter: HomeAssistantConverter,
  private val publisher: HomeAssistantPublisher,
  private val mqttClientFactory: MqttClientFactory
) {

  fun sendHomeAssistantConfiguration(deviceRegistry: DeviceRegistry) {
    LOGGER.info { "Publishing HomeAssistant configuration" }

    val uniGatewayDevice = deviceRegistry.getUniGatewayDevice()
    val mqttClient = mqttClientFactory.create(
      "${uniGatewayDevice.id}-homeassistant-configurator",
      { LOGGER.info { "HomeAssistant configurator connected" } },
      { LOGGER.info { "HomeAssistant configurator disconnected" } }
    )

    mqttClient.connect(MqttMessage("${properties.rootTopic}/state", "disconnected", 0, false), true)

    val components = converter.convert(deviceRegistry)
    publisher.cleanPublishedConfigurations(mqttClient, properties.rootTopic, uniGatewayDevice.id)
    publisher.publish(mqttClient, properties.rootTopic, components)

    mqttClient.disconnect()
    LOGGER.info { "HomeAssistant configuration published" }
  }
}
