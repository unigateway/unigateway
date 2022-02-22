package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class WireUsageValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    // TODO Need to move to be implemented with MqGateway Hardware Interface
    return emptyList()
  }
}
