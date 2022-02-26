package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class ConfigValidator(
  private val yamlObjectMapper: ObjectMapper,
  private val gatewaySystemProperties: GatewaySystemProperties,
  private val validators: List<GatewayValidator>
) {

  fun validateGateway(gatewayConfiguration: GatewayConfiguration): ValidationResult {

    val validationFailureReasons = validators.flatMap { it.validate(gatewayConfiguration, gatewaySystemProperties) }

    return if (validationFailureReasons.isEmpty()) {
      ValidationResult(true)
    } else {
      ValidationResult(false, validationFailureReasons)
    }
  }

  fun validateAgainstJsonSchema(gatewayJsonNode: JsonNode): Boolean {
    LOGGER.debug { "Validation gateway configuration against JSON schema" }

    LOGGER.trace { "Reading JSON schema from file" }
    val schemaNode = yamlObjectMapper.readTree(javaClass.getResourceAsStream("/config.schema.json"))

    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
    val schemaValidator = factory.getSchema(schemaNode)

    LOGGER.trace { "Running validation" }
    val schemaValidationReport = schemaValidator.validate(gatewayJsonNode)

    for (validationMessage in schemaValidationReport) {
      LOGGER.error { "Processing message ${validationMessage.message}" }
    }

    return schemaValidationReport.isEmpty()
  }
}

data class ValidationResult(val succeeded: Boolean, val failureReasons: List<ValidationFailureReason> = emptyList())
