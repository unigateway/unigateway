package com.mqgateway.core.gatewayconfig

import com.fasterxml.jackson.databind.JsonNode
import com.mqgateway.core.gatewayconfig.parser.YamlParser
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import mu.KotlinLogging
import java.io.File
import java.security.MessageDigest

private val LOGGER = KotlinLogging.logger {}

class ConfigLoader(private val yamlParser: YamlParser, private val configValidator: ConfigValidator) {

  companion object {
    private const val HASH_ALGORITHM = "MD5"
    private const val CONFIGURATION_HASH_PATH = ".previousConfig.$HASH_ALGORITHM"
    private const val CONFIGURATION_FILE_QUICK_PATH = ".previousConfig.cbor"
  }

  var configReloaded = false

  fun load(gatewayConfigPath: String): Gateway {
    configReloaded = false
    val gatewayConfigBytes = File(gatewayConfigPath).readBytes()
    val currentConfigurationFileHash = calculateHash(gatewayConfigBytes)
    val storedConfigurationFileHash = loadStoredConfigurationFileHash()
    LOGGER.trace("previousConfigHash=$storedConfigurationFileHash newConfigHash=$currentConfigurationFileHash")
    if (currentConfigurationFileHash == storedConfigurationFileHash) {
      LOGGER.debug { "Configuration file has not changed. Loading configuration from binary store." }
      return Cbor.decodeFromByteArray(File(CONFIGURATION_FILE_QUICK_PATH).readBytes())
    } else {
      LOGGER.info { "New configuration detected. Starting validation." }

      val gatewayJsonNode = yamlParser.toJsonNode(gatewayConfigBytes)
      validateConfigurationAgainstJsonSchema(gatewayJsonNode)
      val gateway = yamlParser.parse(gatewayJsonNode)
      validateGatewayConfigurationValues(gateway)

      File(CONFIGURATION_HASH_PATH).writeText(currentConfigurationFileHash)

      val gatewayCbor: ByteArray = Cbor.encodeToByteArray(gateway)
      File(CONFIGURATION_FILE_QUICK_PATH).writeBytes(gatewayCbor)

      configReloaded = true

      return gateway
    }
  }

  private fun loadStoredConfigurationFileHash(): String? {
    val hashFile = File(CONFIGURATION_HASH_PATH)
    return if (hashFile.isFile) hashFile.readText() else null
  }

  private fun calculateHash(byteArray: ByteArray): String {
    return MessageDigest.getInstance(HASH_ALGORITHM)
      .digest(byteArray)
      .fold("", { str, it -> str + "%02x".format(it) })
  }

  private fun validateConfigurationAgainstJsonSchema(gatewayConfigJsonNode: JsonNode) {
    val validAgainstJsonSchema = configValidator.validateAgainstJsonSchema(gatewayConfigJsonNode)
    if (validAgainstJsonSchema) {
      LOGGER.info { "JSON schema validation succeeded ✔" }
    } else {
      LOGGER.error { "JSON Schema validation failed ✕" }
      throw ValidationFailedException("JSON Schema validation failed. See logs.")
    }
  }

  private fun validateGatewayConfigurationValues(gateway: Gateway) {
    val validationResult = configValidator.validateGateway(gateway)

    if (validationResult.succeeded) {
      LOGGER.info { "Gateway configuration validation succeeded ✔" }
    } else {
      LOGGER.error { "Gateway configuration validation failed ✕" }
      validationResult.failureReasons.forEach {
        LOGGER.error { it.getDescription() }
      }
      throw ValidationFailedException("Gateway configuration validation failed. See logs.")
    }
  }

  class ValidationFailedException(message: String) : Exception(message)
}
