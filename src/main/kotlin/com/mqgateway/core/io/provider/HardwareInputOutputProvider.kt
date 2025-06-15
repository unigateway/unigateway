package com.mqgateway.core.io.provider

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput

/**
 * Provides concrete implementation of input/output interfaces based on hardware
 *
 * @param T concrete implementation class of hardware interface connector
 */
interface HardwareInputOutputProvider<T : HardwareConnector> {
  fun getBinaryInput(connector: T): BinaryInput

  fun getBinaryOutput(connector: T): BinaryOutput

  fun getFloatInput(connector: T): FloatInput

  fun getFloatOutput(connector: T): FloatOutput
}
