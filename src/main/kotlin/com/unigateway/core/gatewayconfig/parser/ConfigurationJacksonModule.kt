package com.unigateway.core.gatewayconfig.parser

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.module.SimpleModule
import com.unigateway.core.gatewayconfig.ConnectorDeserializer
import com.unigateway.core.gatewayconfig.connector.ConnectorFactory
import com.unigateway.core.io.provider.Connector

val VERSION: Version = VersionUtil.parseVersion(
  "1.0.0", "io.unigateway", "unigateway"
)

class ConfigurationJacksonModule(connectorFactory: ConnectorFactory<*>) : SimpleModule("UniGatewayModule", VERSION) {

  init {
    addDeserializer(Connector::class.java, ConnectorDeserializer(connectorFactory))
  }
}
