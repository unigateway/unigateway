package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class PortNumbersRangeValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    // TODO need to move somewhere to be implemented with MqGateway Hardware Interface
    return emptyList()
  }

  companion object {
    fun maxPortNumber(expanderEnabled: Boolean) = if (expanderEnabled) 32 else 16
  }
}
