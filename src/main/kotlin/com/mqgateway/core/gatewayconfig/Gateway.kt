package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class Gateway(
  val configVersion: String,
  val name: String,
  val mqttHostname: String,
  val rooms: List<Room>
)
