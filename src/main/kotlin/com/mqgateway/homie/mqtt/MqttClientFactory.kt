package com.mqgateway.homie.mqtt

interface MqttClientFactory {
  fun create(clientId: String, connectedListener: () -> Unit, disconnectedListener: () -> Unit): MqttClient
}