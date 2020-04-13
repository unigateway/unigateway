package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.core.gatewayconfig.Gateway

class YamlParser(private val objectMapper: ObjectMapper) {

  fun toJsonNode(byteArray: ByteArray): JsonNode {
    return objectMapper.readTree(byteArray)
  }

  fun parse(jsonNode: JsonNode): Gateway {
    return objectMapper.treeToValue(jsonNode, Gateway::class.java)
  }
}
