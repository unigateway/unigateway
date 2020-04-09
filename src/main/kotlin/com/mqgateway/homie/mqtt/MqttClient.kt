package com.mqgateway.homie.mqtt

interface MqttClient {
  fun connect(willMessage: MqttMessage, cleanSession: Boolean = false)
  fun publishSync(mqttMessage: MqttMessage)
  fun subscribeAsync(topicFilter: String, callback: (MqttMessage) -> Unit)
  fun publishAsync(mqttMessage: MqttMessage)
}

data class MqttMessage(
    val topic: String,
    val payload: String,
    val qos: Int = 0,
    val retain: Boolean = false
)