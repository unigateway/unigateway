package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.module.SimpleModule
import com.mqgateway.core.gatewayconfig.ConnectorDeserializer
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.io.provider.HardwareConnector

val VERSION: Version = VersionUtil.parseVersion(
    "1.0.0", "com.unigateway", "unigateway"
)

class ConfigurationJacksonModule<T : HardwareConnector>(connectorFactory: ConnectorFactory<T>) : SimpleModule("UniGatewayModule", VERSION) {

    init {
        addDeserializer(Connector::class.java, ConnectorDeserializer(connectorFactory))
    }

}
