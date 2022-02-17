package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class InternalDeviceConfiguration(
  val referenceId: String
)
