package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

interface GatewayValidator {
  fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason>
}

class EmptyGatewayValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    LOGGER.info { "Empty gateway validator - always passing" }
    return emptyList()
  }
}

abstract class ValidationFailureReason {
  abstract fun getDescription(): String
}
