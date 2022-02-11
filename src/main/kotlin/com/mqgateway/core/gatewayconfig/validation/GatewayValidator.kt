package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration

interface GatewayValidator {
  fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason>
}

abstract class ValidationFailureReason {
  abstract fun getDescription(): String
}
