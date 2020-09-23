package com.mqgateway.core.gatewayconfig.homeassistant

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttMessage
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantPublisher(
  private val objectMapper: ObjectMapper
) {

  fun publish(mqttClient: MqttClient, rootTopic: String, components: List<HomeAssistantComponent>) {
    components.map { component ->
      val topic = "$rootTopic/${component.componentType.value}/${component.properties.nodeId}/${component.properties.objectId}/config"
      val payload = objectMapper.writeValueAsString(component)
      MqttMessage(topic, payload, 1, true)
    }.forEach { mqttMessage ->
      LOGGER.debug { "Publishing MQTT message: $mqttMessage" }
      mqttClient.publishSync(mqttMessage)
    }
  }
}
