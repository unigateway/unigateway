package com.unigateway.homie

class MqttStatusIndicator : HomieDevice.MqttConnectionListener {

  var isConnected = false
    private set

  override fun onConnected() {
    isConnected = true
  }

  override fun onDisconnect() {
    isConnected = false
  }
}
