package com.unigateway.core.hardware.simulated

import com.unigateway.core.io.provider.HardwareConnector
import kotlinx.serialization.Serializable

@Serializable
data class SimulatedConnector @JvmOverloads constructor(
  val pin: Int,
  val initialValue: String? = null
) : HardwareConnector
