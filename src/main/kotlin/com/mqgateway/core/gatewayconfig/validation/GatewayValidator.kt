package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.gatewayconfig.Gateway

interface GatewayValidator {
  fun validate(gateway: Gateway): List<ValidationFailureReason>
}

abstract class ValidationFailureReason {
  abstract fun getDescription(): String
}
