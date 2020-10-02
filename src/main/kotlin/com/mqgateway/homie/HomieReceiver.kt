package com.mqgateway.homie

interface HomieReceiver {
  fun initProperty(nodeId: String, propertyId: String, value: String)
  fun propertySet(mqttTopic: String, payload: String)
}
