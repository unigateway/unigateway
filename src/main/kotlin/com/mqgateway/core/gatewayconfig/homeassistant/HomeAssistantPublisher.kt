package com.mqgateway.core.gatewayconfig.homeassistant

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttMessage
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantPublisher(
  private val objectMapper: ObjectMapper,
) {
  fun cleanPublishedConfigurations(
    mqttClient: MqttClient,
    rootTopic: String,
    commonNodeId: String,
  ) {
    HomeAssistantComponentType.values()
      .map { it.value }
      .flatMap { haComponentType ->
        mqttClient.findAllSubtopicsWithRetainedMessages("$rootTopic/$haComponentType/$commonNodeId")
      }.forEach { topic ->
        LOGGER.debug { "Removing HomeAssistant config from topic: $topic" }
        mqttClient.publishSync(MqttMessage(topic, ""))
      }
  }

  fun publish(
    mqttClient: MqttClient,
    rootTopic: String,
    components: List<HomeAssistantComponent>,
  ) {
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
