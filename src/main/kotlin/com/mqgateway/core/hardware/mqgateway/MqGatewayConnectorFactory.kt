package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory

class MqGatewayConnectorFactory : HardwareConnectorFactory<MqGatewayConnector> {

  override fun create(config: Map<String, *>): MqGatewayConnector {
    validateConfig(config)

    return MqGatewayConnector(
      config[PORT_NUMBER_KEY] as Int,
      WireColor.valueOf(config[WIRE_COLOR_KEY] as String),
      config[DEBOUNCE_MS_KEY]?.toString()?.toInt()
    )
  }

  private fun validateConfig(config: Map<String, *>) {
    REQUIRED_FIELDS
      .filter { !config.containsKey(it) }
      .forEach { throw MissingConnectorConfigurationException(it) }
  }

  companion object {
    private const val PORT_NUMBER_KEY = "portNumber"
    private const val WIRE_COLOR_KEY = "wireColor"
    private const val DEBOUNCE_MS_KEY = "debounceMs"
    private val REQUIRED_FIELDS = listOf(PORT_NUMBER_KEY, WIRE_COLOR_KEY)
  }
}

class MissingConnectorConfigurationException(missingFieldName: String) :
  RuntimeException("Missing configuration field $missingFieldName for connector")
