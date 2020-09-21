package com.mqgateway.homie.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttClient as PahoClient

private val LOGGER = KotlinLogging.logger {}

class PahoMqttClient(private val mqttClient: PahoClient) : MqttClient {

  override fun connect(willMessage: MqttMessage, cleanSession: Boolean) {
    val options = MqttConnectOptions()
    options.maxInflight = 100
    options.isCleanSession = cleanSession
    options.setWill(willMessage.topic, willMessage.payload.toByteArray(), willMessage.qos, willMessage.retain)
    options.isAutomaticReconnect = true
    mqttClient.connect(options)
  }

  fun onConnectComplete(onConnectComplete: (reconnected: Boolean) -> Unit) {
    mqttClient.setCallback(PahoMqttCallback(onConnectComplete))
  }

  override fun publishSync(mqttMessage: MqttMessage) {
    LOGGER.info { "Publishing message ${mqttMessage.topic} ${mqttMessage.payload} (QoS: ${mqttMessage.qos})" }
    mqttClient.publish(mqttMessage.topic, mqttMessage.payload.toByteArray(), mqttMessage.qos, mqttMessage.retain)
    LOGGER.trace { "Message published" }
  }

  override fun publishAsync(mqttMessage: MqttMessage) {
    publishSync(mqttMessage)
  }

  override fun disconnect() {
    mqttClient.disconnect()
  }

  override fun subscribeAsync(topicFilter: String, callback: (MqttMessage) -> Unit) {
    mqttClient.subscribe(topicFilter) { topic, message ->
      LOGGER.info { "Message received on $topic ${String(message.payload)} (qos: ${message.qos})" }
      callback(MqttMessage(topic, String(message.payload)))
      LOGGER.trace { "Callback on received message processed" }
    }
  }
}

class PahoMqttCallback(
  private val onConnectComplete: (reconnected: Boolean) -> Unit = {},
  private val onConnectionLost: (cause: Throwable?) -> Unit = {}
) : MqttCallbackExtended {

  override fun connectComplete(reconnect: Boolean, serverURI: String?) {
    LOGGER.debug { "Connect complete. Calling onConnectComplete." }
    onConnectComplete(reconnect)
    LOGGER.debug { "onConnectComplete finished" }
  }

  override fun messageArrived(topic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
    // not needed
  }

  override fun connectionLost(cause: Throwable?) {
    onConnectionLost(cause)
  }

  override fun deliveryComplete(token: IMqttDeliveryToken?) {
    // not needed
  }
}
