package com.mqgateway.homie

interface HomieReceiver {
  fun propertySet(mqttTopic: String, payload: String)
}