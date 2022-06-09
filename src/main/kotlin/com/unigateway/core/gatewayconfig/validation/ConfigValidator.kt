package com.unigateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class ConfigValidator(
  private val jsonSchemaValidator: JsonSchemaValidator,
  private val validators: List<GatewayValidator>,
) {

  fun validateGateway(gatewayConfiguration: GatewayConfiguration): ValidationResult {
    val validationFailureReasons = validators.flatMap { it.validate(gatewayConfiguration) }

    return if (validationFailureReasons.isEmpty()) {
      ValidationResult(true)
    } else {
      ValidationResult(false, validationFailureReasons)
    }
  }

  fun validateAgainstJsonSchema(gatewayJsonNode: JsonNode): Boolean {
    LOGGER.debug { "Validation gateway configuration against JSON schema" }

    LOGGER.trace { "Running validation" }
    val schemaValidationReport = jsonSchemaValidator.validate(gatewayJsonNode)

    for (validationMessage in schemaValidationReport) {
      LOGGER.error { "Processing message ${validationMessage.message}" }
    }

    return schemaValidationReport.isEmpty()
  }
}

data class ValidationResult(val succeeded: Boolean, val failureReasons: List<ValidationFailureReason> = emptyList())
