package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.Point
import jakarta.inject.Singleton

@Singleton
class PortNumbersRangeValidator : GatewayValidator {

  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val maxPortNumber = maxPortNumber(systemProperties.expander.enabled)
    val pointWithWrongPortNumber: List<Point> = gateway.rooms.flatMap { room -> room.points }.filter { point -> point.portNumber > maxPortNumber }
    return pointWithWrongPortNumber.map { PortNumberOutOfRange(it, systemProperties.expander.enabled) }
  }

  companion object {
    fun maxPortNumber(expanderEnabled: Boolean) = if (expanderEnabled) 32 else 16
  }

  class PortNumberOutOfRange(val point: Point, private val expanderEnabled: Boolean) : ValidationFailureReason() {

    override fun getDescription(): String {
      val expanderDisabledInfo = if (!expanderEnabled) " It may be because expander is disabled in system configuration." else ""
      return "Point '${point.name}' has port number '${point.portNumber}' which more then max (${maxPortNumber(expanderEnabled)})." +
        expanderDisabledInfo
    }
  }
}
