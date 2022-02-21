package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.provider.HardwareConnector

data class SimulatedConnector(
  val pin: Int
) : HardwareConnector
