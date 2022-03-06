package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.module.SimpleModule
import com.mqgateway.core.gatewayconfig.ConnectorDeserializer
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.io.provider.Connector

val VERSION: Version = VersionUtil.parseVersion(
  "1.0.0", "io.unigateway", "unigateway"
)

class ConfigurationJacksonModule(connectorFactory: ConnectorFactory<*>) : SimpleModule("UniGatewayModule", VERSION) {

  init {
    addDeserializer(Connector::class.java, ConnectorDeserializer(connectorFactory))
  }
}
