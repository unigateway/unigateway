package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.io.provider.HardwareConnector
import kotlinx.serialization.Serializable

@Serializable
data class MqGatewayConnector(
  val portNumber: Int,
  val wireColor: WireColor,
  val debounceMs: Long?
) : HardwareConnector

enum class WireColor(val number: Int) {
  ORANGE_WHITE(1),
  ORANGE(2),
  GREEN_WHITE(3),
  BLUE(4),
  BLUE_WHITE(5),
  GREEN(6),
  BROWN_WHITE(7),
  BROWN(8)
}
