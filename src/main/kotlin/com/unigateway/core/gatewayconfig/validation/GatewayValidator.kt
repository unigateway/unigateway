package com.unigateway.core.gatewayconfig.validation

import com.unigateway.core.gatewayconfig.GatewayConfiguration
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

interface GatewayValidator {
  fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason>
}

class EmptyGatewayValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    LOGGER.info { "Empty gateway validator - always passing" }
    return emptyList()
  }
}

abstract class ValidationFailureReason {
  abstract fun getDescription(): String
}
