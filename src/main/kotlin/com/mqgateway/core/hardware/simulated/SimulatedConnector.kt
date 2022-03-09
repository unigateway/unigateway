package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.provider.HardwareConnector

data class SimulatedConnector @JvmOverloads constructor(
  val pin: Int,
  val initialValue: String? = null
) : HardwareConnector
