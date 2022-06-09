package com.unigateway.core.gatewayconfig

import com.unigateway.core.device.DeviceType
import com.unigateway.core.io.provider.Connector
import kotlinx.serialization.Serializable

@Serializable
data class DeviceConfiguration
@JvmOverloads constructor(
  val id: String,
  val name: String,
  val type: DeviceType,
  val connectors: Map<String, Connector> = emptyMap(),
  val internalDevices: Map<String, InternalDeviceConfiguration> = emptyMap(),
  val config: Map<String, String> = emptyMap()
)

@Serializable
data class InternalDeviceConfiguration(
  val referenceId: String
)
