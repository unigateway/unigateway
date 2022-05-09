package com.mqgateway.core.gatewayconfig.connector

import com.mqgateway.core.io.provider.MySensorsConnector
import com.mqgateway.core.mysensors.InternalType
import com.mqgateway.core.mysensors.PresentationType
import com.mqgateway.core.mysensors.SetReqType
import com.mqgateway.core.mysensors.StreamType
import com.mqgateway.core.mysensors.Type

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
      type.startsWith("S_") -> PresentationType.valueOf(type)
      type.startsWith("I_") -> InternalType.valueOf(type)
      type.startsWith("V_") -> SetReqType.valueOf(type)
      type == "STREAM" -> StreamType.valueOf(type)
      else -> throw RuntimeException("Type not supported")
    }
  }
}
