package com.mqgateway.hwimpl

import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.BinaryState
import com.mqgateway.core.hardware.io.BinaryStateListener
import com.mqgateway.core.hardware.io.FloatInput
import com.mqgateway.core.hardware.provider.ConnectorConfiguration
import com.mqgateway.core.hardware.provider.HardwareInputOutputProvider
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

// TODO implement properly - this is now only to make everything compile
class SimulatedInputOutputProvider : HardwareInputOutputProvider {

  override fun getBinaryInput(connectorConfiguration: ConnectorConfiguration): BinaryInput {
    return SimulatedBinaryInput(connectorConfiguration as SimulatedConnectorConfiguration)
  }

  override fun getBinaryOutput(connectorConfiguration: ConnectorConfiguration): BinaryOutput {
    return SimulatedBinaryOutput(connectorConfiguration as SimulatedConnectorConfiguration)
  }

  override fun getFloatInput(connectorConfiguration: ConnectorConfiguration): FloatInput {
    TODO("Not yet implemented")
  }
}

data class SimulatedConnectorConfiguration(
  val pinNumber: Int,
  val debounceMs: Int = 0
) : ConnectorConfiguration

class SimulatedBinaryInput(private val config: SimulatedConnectorConfiguration) : BinaryInput {

  override fun addListener(listener: BinaryStateListener) {
    LOGGER.info { "Listener added to pin ${config.pinNumber}" }
  }

  override fun getState() = BinaryState.HIGH
}

class SimulatedBinaryOutput(private val config: SimulatedConnectorConfiguration) : BinaryOutput {
  override fun setState(state: BinaryState) {
    LOGGER.info { "State set to $state on pin ${config.pinNumber}" }
  }
}

