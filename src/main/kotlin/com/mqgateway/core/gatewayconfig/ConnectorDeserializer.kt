package com.mqgateway.core.gatewayconfig

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.io.provider.Connector

class ConnectorDeserializer(
  private val connectorFactory: ConnectorFactory<*>
) : StdDeserializer<Connector>(Connector::class.java) {

  @Suppress("UNCHECKED_CAST")
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Connector {
    val parsedMap = (p.codec.readValue(p, Map::class.java)) as Map<String, *>
    return connectorFactory.create(parsedMap)
  }
}
