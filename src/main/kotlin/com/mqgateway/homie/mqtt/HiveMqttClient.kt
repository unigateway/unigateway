package com.mqgateway.homie.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.exceptions.MqttClientStateException as HiveMqttClientStateException

class HiveMqttClient(private val mqttClient: Mqtt3BlockingClient) : MqttClient {
  override fun connect(
    willMessage: MqttMessage,
    cleanSession: Boolean,
  ) {
    mqttClient.connectWith()
      .cleanSession(cleanSession)
      .willPublish()
      .topic(willMessage.topic)
      .payload(willMessage.payload.toByteArray())
      .qos(MqttQos.fromCode(willMessage.qos)!!)
      .retain(willMessage.retain)
      .applyWillPublish()
      .send()
  }

  override fun publishSync(mqttMessage: MqttMessage) {
    try {
      mqttClient.publishWith()
        .topic(mqttMessage.topic)
        .payload(mqttMessage.payload.toByteArray())
        .retain(mqttMessage.retain)
        .qos(MqttQos.fromCode(mqttMessage.qos)!!)
        .send()
    } catch (e: HiveMqttClientStateException) {
      throw MqttClientStateException("Failed to publish MQTT message", e)
    }
  }

  override fun publishAsync(mqttMessage: MqttMessage) {
    mqttClient.toAsync()
      .publishWith()
      .topic(mqttMessage.topic)
      .payload(mqttMessage.payload.toByteArray())
      .retain(mqttMessage.retain)
      .qos(MqttQos.fromCode(mqttMessage.qos)!!)
      .send()
  }

  override fun read(topic: String): String? {
    var result: String? = null
    subscribeAsync(topic) { result = it.payload }
    Thread.sleep(READING_WAIT_TIME_MS)
    mqttClient.unsubscribeWith().topicFilter(topic).send()
    return result
  }

  override fun findAllSubtopicsWithRetainedMessages(startTopic: String): Set<String> {
    val topics = mutableSetOf<String>()
    subscribeAsync("$startTopic/#") { topics.add(it.topic) }
    Thread.sleep(READING_WAIT_TIME_MS)
    mqttClient.unsubscribeWith().topicFilter("$startTopic/#").send()
    return topics
  }

  override fun subscribeAsync(
    topicFilter: String,
    callback: (MqttMessage) -> Unit,
  ) {
    mqttClient.toAsync()
      .subscribeWith().topicFilter(topicFilter)
      .callback { callback(MqttMessage(it.topic.toString(), String(it.payloadAsBytes), it.qos.code, it.isRetain)) }
      .send()
  }

  override fun disconnect() {
    mqttClient.disconnect()
  }

  companion object {
    const val READING_WAIT_TIME_MS = 50L
  }
}
