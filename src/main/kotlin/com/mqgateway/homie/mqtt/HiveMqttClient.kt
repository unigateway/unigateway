package com.mqgateway.homie.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient

class HiveMqttClient(private val mqttClient: Mqtt3BlockingClient): MqttClient {

  override fun connect(willMessage: MqttMessage, cleanSession: Boolean) {
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
    mqttClient.publishWith()
        .topic(mqttMessage.topic)
        .payload(mqttMessage.payload.toByteArray())
        .retain(mqttMessage.retain)
        .qos(MqttQos.fromCode(mqttMessage.qos)!!)
        .send()
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

  override fun subscribeAsync(topicFilter: String, callback: (MqttMessage) -> Unit) {
    mqttClient.toAsync()
        .subscribeWith().topicFilter(topicFilter)
        .callback { callback(MqttMessage(it.topic.toString(), String(it.payloadAsBytes))) }
        .send()
  }

}