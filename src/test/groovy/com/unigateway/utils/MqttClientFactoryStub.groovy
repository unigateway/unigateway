package com.unigateway.utils


import kotlin.Unit
import kotlin.jvm.functions.Function0
import org.jetbrains.annotations.NotNull
import com.unigateway.homie.mqtt.MqttClient
import com.unigateway.homie.mqtt.MqttClientFactory

class MqttClientFactoryStub implements com.unigateway.homie.mqtt.MqttClientFactory {

  MqttClientStub mqttClient

  private final boolean sameClient

  MqttClientFactoryStub(boolean sameClient = false) {
    this.sameClient = sameClient
  }

  @Override
  com.unigateway.homie.mqtt.MqttClient create(@NotNull String clientId = "client", @NotNull Function0<Unit> connectedListener = {}, @NotNull Function0<Unit> disconnectedListener = {}) {
    def listener = new TestMqttConnectionListener() {

      @Override
      def onConnected() {
        connectedListener.invoke()
      }

      @Override
      def onDisconnected() {
        disconnectedListener.invoke()
      }
    }
    if (!sameClient || !this.mqttClient) {
      this.mqttClient = new MqttClientStub([listener])
    }
    return mqttClient
  }
}
