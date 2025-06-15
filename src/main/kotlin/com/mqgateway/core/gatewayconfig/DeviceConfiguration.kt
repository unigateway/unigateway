package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.provider.Connector
import kotlinx.serialization.Serializable

@Serializable
data class DeviceConfiguration
  @JvmOverloads
  constructor(
    val id: String,
    val name: String,
    val type: DeviceType,
    val connectors: Map<String, Connector> = emptyMap(),
    val internalDevices: Map<String, InternalDeviceConfiguration> = emptyMap(),
    val config: Map<String, String> = emptyMap(),
  )

@Serializable
data class InternalDeviceConfiguration(
  val referenceId: String,
)
