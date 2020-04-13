package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.LogLevel
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.mqgateway.core.gatewayconfig.Gateway
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class ConfigValidator(private val yamlObjectMapper: ObjectMapper) {

  private val validators =
      listOf(
          UniqueDeviceIdsValidator(),
          UniquePortNumbersForPointsValidator(),
          DeviceNameValidator(),
          WireUsageValidator()
      )

  fun validateGateway(gateway: Gateway): ValidationResult {

    val validationFailureReasons = validators.flatMap { it.validate(gateway) }

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

    val factory = JsonSchemaFactory.byDefault()
    val schemaValidator = factory.getJsonSchema(schemaNode)

    LOGGER.trace { "Running validation" }
    val schemaValidationReport = schemaValidator.validate(gatewayJsonNode)

    for (processingMessage in schemaValidationReport) {
      if (processingMessage.logLevel in listOf(LogLevel.ERROR, LogLevel.FATAL)) {
        LOGGER.error { "Processing message ${processingMessage.message}" }
      }
    }

    return schemaValidationReport.isSuccess
  }
}

data class ValidationResult(val succeeded: Boolean, val failureReasons: List<ValidationFailureReason> = emptyList())
