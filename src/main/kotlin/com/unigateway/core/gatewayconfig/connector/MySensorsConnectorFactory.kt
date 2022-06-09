package com.unigateway.core.gatewayconfig.connector

import com.unigateway.core.io.provider.MySensorsConnector
import com.unigateway.core.mysensors.SetReqType
import com.unigateway.core.mysensors.Type

class MySensorsConnectorFactory {

  /**
   * @param config connector config read from device config section
   */
  fun create(config: Map<String, *>): MySensorsConnector {
    return MySensorsConnector(
      config["nodeId"] as Int,
      config["sensorId"] as Int,
      parseType(config["type"] as String)
    )
  }

  private fun parseType(type: String): Type {
    return when {
      type.startsWith("V_") -> SetReqType.valueOf(type)
      else -> throw MySensorsEventTypeNotSupported(type)
    }
  }
}

class MySensorsEventTypeNotSupported(type: String) :
  Exception("Parsed type is not supported: $type. Only type for set, req is possible to set (prefix: V_)")
