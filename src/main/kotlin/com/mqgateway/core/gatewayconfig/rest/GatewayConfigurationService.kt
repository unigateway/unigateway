package com.mqgateway.core.gatewayconfig.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import com.mqgateway.core.gatewayconfig.validation.ValidationFailureReason
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val LOGGER = KotlinLogging.logger {}

class GatewayConfigurationService(
  private val configValidator: ConfigValidator,
  private val gatewayConfigPath: String,
  private val yamlObjectMapper: ObjectMapper
) {

  fun replaceConfiguration(newConfigurationString: String): GatewayConfigurationReplacementResult {

    val newConfigurationJsonNode: JsonNode = yamlObjectMapper.readTree(newConfigurationString)
    if (!configValidator.validateAgainstJsonSchema(newConfigurationJsonNode)) {
      LOGGER.warn { "New Gateway configuration is invalid (against JSON schema)" }
      LOGGER.info { "Nothing has been changed. MqGateway will continue to work with previous configuration" }
      return GatewayConfigurationReplacementResult(false, jsonValidationSucceeded = false)
    }

    val newConfiguration: Gateway = yamlObjectMapper.readValue(newConfigurationString, Gateway::class.java)
    val validationResult = configValidator.validateGateway(newConfiguration)
    if (validationResult.succeeded) {
      LOGGER.info { "New Gateway configuration validation succeeded ✔" }
    } else {
      LOGGER.warn { "New Gateway configuration validation failed ✕" }
      validationResult.failureReasons.forEach {
        LOGGER.warn { it.getDescription() }
      }
      LOGGER.info { "Nothing has been changed. MqGateway will continue to work with previous configuration" }
      return GatewayConfigurationReplacementResult(
        success = false,
        jsonValidationSucceeded = true,
        validationFailures = validationResult.failureReasons
      )
    }

    LOGGER.info { "New Gateway configuration has been verified and will replace old configuration" }
    Files.copy(Paths.get(gatewayConfigPath), Paths.get(gatewayConfigPath + OLD_CONFIGURATION_FILE_POSTFIX), StandardCopyOption.REPLACE_EXISTING)
    File(gatewayConfigPath).writeText(newConfigurationString)

    thread {
      Thread.sleep(1000)
      LOGGER.warn { "Gateway configuration replaced - MqGateway will close now to restart..." }
      exitProcess(0)
    }

    return GatewayConfigurationReplacementResult(true)
  }

  companion object {
    private const val OLD_CONFIGURATION_FILE_POSTFIX = ".old"
  }
}

data class GatewayConfigurationReplacementResult(
  val success: Boolean,
  val jsonValidationSucceeded: Boolean = true,
  val validationFailures: List<ValidationFailureReason> = emptyList()
)
