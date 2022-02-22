package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class GatewayConfiguration(
  val configVersion: String,
  val name: String,
  val devices: List<DeviceConfiguration>
)
