package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.Point
import jakarta.inject.Singleton

@Singleton
class PortNumbersRangeValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    return emptyList()
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
