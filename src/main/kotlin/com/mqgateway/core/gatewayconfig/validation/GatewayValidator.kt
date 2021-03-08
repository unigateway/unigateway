package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.Gateway

interface GatewayValidator {
  fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason>
}

abstract class ValidationFailureReason {
  abstract fun getDescription(): String
}
