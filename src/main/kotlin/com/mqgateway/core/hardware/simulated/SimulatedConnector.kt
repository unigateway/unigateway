package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.provider.HardwareConnector
import kotlinx.serialization.Serializable

@Serializable
data class SimulatedConnector
  @JvmOverloads
  constructor(
    val pin: Int,
    val initialValue: String? = null,
  ) : HardwareConnector
