package com.mqgateway.homie.mqtt

import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import java.util.concurrent.TimeUnit

class HiveMqttClientFactory(private val mqttServerHost: String) : MqttClientFactory {

  override fun create(clientId: String, connectedListener: () -> Unit, disconnectedListener: () -> Unit): MqttClient {
    val hiveClient: Mqtt3BlockingClient = Mqtt3Client.builder()
      .automaticReconnect().initialDelay(100, TimeUnit.MILLISECONDS).maxDelay(1, TimeUnit.SECONDS).applyAutomaticReconnect()
      .identifier(clientId)
      .addDisconnectedListener { disconnectedListener() }
      .addConnectedListener { connectedListener() }
      .serverHost(mqttServerHost)
      .buildBlocking()

    return HiveMqttClient(hiveClient)
  }
}
