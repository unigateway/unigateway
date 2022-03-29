package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.DeviceType
import kotlinx.serialization.Serializable

@Serializable
data class GatewayConfiguration(
  val configVersion: String,
  val id: String,
  val name: String,
  val devices: List<DeviceConfiguration>
) {
  fun deviceById(id: String): DeviceConfiguration? = devices.find { it.id == id }
  fun devicesByType(type: DeviceType): List<DeviceConfiguration> = devices.filter { it.type == type }
}
