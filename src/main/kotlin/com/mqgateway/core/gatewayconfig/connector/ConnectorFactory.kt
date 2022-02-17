package com.mqgateway.core.gatewayconfig.connector

import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.io.provider.HardwareConnector

/**
 * Provides concrete connector implementation based on the given configuration. It can be either hardware-based or
 * different (e.g. MySensors) representation
 *
 * @param T concrete implementation class of hardware interface connector
 */
class ConnectorFactory<T : HardwareConnector>(
  private val mySensorsConnectorFactory: MySensorsConnectorFactory,
  private val hardwareConnectorFactory: HardwareConnectorFactory<T>
) {

  /**
   * @param config connector config read from device config section
   */
  fun create(config: Map<String, *>): Connector {
    val source: Source = Source.valueOf(config[SOURCE_KEY] as String? ?: Source.HARDWARE.name)
    return when (source) {
      Source.HARDWARE -> hardwareConnectorFactory.create(config)
      Source.MYSENSORS -> mySensorsConnectorFactory.create(config)
    }
  }

  companion object {
    private const val SOURCE_KEY = "source"
  }

  private enum class Source {
    HARDWARE, MYSENSORS
  }
}
