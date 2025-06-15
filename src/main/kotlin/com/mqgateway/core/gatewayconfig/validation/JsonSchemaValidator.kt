package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.GatewaySystemProperties
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class JsonSchemaValidator(
  private val yamlObjectMapper: ObjectMapper,
  private val gatewaySystemProperties: GatewaySystemProperties,
) {
  fun validate(gatewayJsonNode: JsonNode): Set<ValidationMessage> {
    LOGGER.trace { "Reading JSON schema from file" }
    val schemaNode = yamlObjectMapper.readTree(javaClass.getResourceAsStream(JSON_SCHEMA_RESOURCE_NAME))

    val factory =
      JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7, { builder: JsonSchemaFactory.Builder ->
        builder.schemaMappers { schemaMappers ->
          schemaMappers.mappings(createConnectorMapping(gatewaySystemProperties.platform))
        }
      })

    val jsonSchema = factory.getSchema(schemaNode)

    LOGGER.trace { "Running validation" }
    return jsonSchema.validate(gatewayJsonNode)
  }

  private fun createConnectorMapping(platform: String): Map<String, String> {
    val fileName = "connector-${platform.lowercase()}.schema.json"
    val uri = "classpath:/$fileName"
    return javaClass.getResource("/$fileName")?.let {
      LOGGER.trace { "Configuring JSON Schema for hardware specific connector. Resource: $uri" }
      mapOf(CONNECTOR_URI to uri)
    } ?: run {
      LOGGER.trace { "JSON Schema for hardware specific connector does not exist. Looking for: $uri" }
      emptyMap()
    }
  }

  companion object {
    private const val JSON_SCHEMA_RESOURCE_NAME: String = "/config.schema.json"
    private const val CONNECTOR_URI: String = "classpath:/default-connector.schema.json"
  }
}
