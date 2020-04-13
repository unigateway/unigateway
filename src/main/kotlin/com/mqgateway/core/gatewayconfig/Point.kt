package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class Point(
  val name: String,
  val portNumber: Int,
  val devices: List<DeviceConfig>
)
