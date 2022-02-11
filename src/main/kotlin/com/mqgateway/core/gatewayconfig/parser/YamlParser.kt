package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.core.gatewayconfig.GatewayConfiguration

class YamlParser(private val objectMapper: ObjectMapper) {

  fun toJsonNode(byteArray: ByteArray): JsonNode {
    return objectMapper.readTree(byteArray)
  }

  fun parse(jsonNode: JsonNode): GatewayConfiguration {
    return objectMapper.treeToValue(jsonNode, GatewayConfiguration::class.java)
  }
}
