package com.mqgateway.core.io.provider

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput

/**
 * Provides concrete implementation of input/output interfaces
 *
 * @param T concrete implementation class of hardware interface connector
 */
class InputOutputProvider<T : HardwareConnector>(
  private val hardwareInputOutputProvider: HardwareInputOutputProvider<T>,
  private val mySensorsInputOutputProvider: MySensorsInputOutputProvider,
) {
  @Suppress("UNCHECKED_CAST")
  fun getBinaryInput(connector: Connector): BinaryInput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getBinaryInput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getBinaryInput(connector as T)
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun getBinaryOutput(connector: Connector): BinaryOutput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getBinaryOutput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getBinaryOutput(connector as T)
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun getFloatInput(connector: Connector): FloatInput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getFloatInput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getFloatInput(connector as T)
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun getFloatOutput(connector: Connector): FloatOutput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getFloatOutput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getFloatOutput(connector as T)
    }
  }
}
