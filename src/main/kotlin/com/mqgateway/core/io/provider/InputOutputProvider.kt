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
  private val mySensorsInputOutputProvider: MySensorsInputOutputProvider
) {

  fun getBinaryInput(connector: Connector): BinaryInput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getBinaryInput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getBinaryInput(connector as T)
    }
  }

  fun getBinaryOutput(connector: Connector): BinaryOutput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getBinaryOutput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getBinaryOutput(connector as T)
    }
  }

  fun getFloatInput(connector: Connector): FloatInput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getFloatInput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getFloatInput(connector as T)
    }
  }

  fun getFloatOutput(connector: Connector): FloatOutput {
    return when (connector) {
      is MySensorsConnector -> mySensorsInputOutputProvider.getFloatOutput(connector)
      is HardwareConnector -> hardwareInputOutputProvider.getFloatOutput(connector as T)
    }
  }
}
