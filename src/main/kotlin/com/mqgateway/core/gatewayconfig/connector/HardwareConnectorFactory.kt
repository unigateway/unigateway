package com.mqgateway.core.gatewayconfig.connector

import com.mqgateway.core.io.provider.HardwareConnector

/**
 * Provides concrete connector for hardware interface implementation, based on the given configuration.
 *
 * @param T concrete implementation class of hardware interface connector
 */
interface HardwareConnectorFactory<out T : HardwareConnector> {
  /**
   * @param config connector config read from device config section
   */
  fun create(config: HashMap<String, Any>): T
}
