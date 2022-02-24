package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class GatewayConfiguration(
  val configVersion: String,
  val name: String,
  val devices: List<DeviceConfiguration>
) {
  fun deviceById(id: String): DeviceConfiguration? = devices.find { it.id == id }
  fun devicesByType(type: DeviceType): List<DeviceConfiguration> = devices.filter { it.type == type }
}
