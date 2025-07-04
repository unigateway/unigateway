package com.mqgateway.homie.mqtt

import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import java.util.concurrent.TimeUnit

class HiveMqttClientFactory(
  private val mqttServerHost: String,
  private val mqttServerPort: Int = 1883,
  private val username: String? = null,
  private val password: String? = null,
) : MqttClientFactory {
  override fun create(
    clientId: String,
    connectedListener: () -> Unit,
    disconnectedListener: () -> Unit,
  ): MqttClient {
    var hiveClientBuilder =
      Mqtt3Client.builder()
        .automaticReconnect().initialDelay(100, TimeUnit.MILLISECONDS).maxDelay(1, TimeUnit.SECONDS).applyAutomaticReconnect()
        .identifier(clientId)
        .addDisconnectedListener { disconnectedListener() }
        .addConnectedListener { connectedListener() }
        .serverHost(mqttServerHost)
        .serverPort(mqttServerPort)

    if (username != null && password != null) {
      hiveClientBuilder =
        hiveClientBuilder.simpleAuth()
          .username(username)
          .password(password.toByteArray(Charsets.UTF_8))
          .applySimpleAuth()
    }

    val hiveClient: Mqtt3BlockingClient =
      hiveClientBuilder
        .buildBlocking()

    return HiveMqttClient(hiveClient)
  }
}
