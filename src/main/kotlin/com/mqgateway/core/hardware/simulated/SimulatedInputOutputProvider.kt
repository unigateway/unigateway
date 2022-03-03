package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class SimulatedInputOutputProvider(private val platformConfiguration: SimulatedPlatformConfiguration) : HardwareInputOutputProvider<SimulatedConnector> {

  override fun getBinaryInput(connector: SimulatedConnector): SimulatedBinaryInput {
    return SimulatedBinaryInput(connector.initialValue?.let { BinaryState.valueOf(it) } ?: BinaryState.HIGH)
  }

  override fun getBinaryOutput(connector: SimulatedConnector): SimulatedBinaryOutput {
    return SimulatedBinaryOutput()
  }

  override fun getFloatInput(connector: SimulatedConnector): SimulatedFloatInput {
    return SimulatedFloatInput(connector.initialValue?.toFloat() ?: 0f)
  }

  override fun getFloatOutput(connector: SimulatedConnector): SimulatedFloatOutput {
    return SimulatedFloatOutput()
  }
}
