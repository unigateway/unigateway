package com.mqgateway.core.hardware.provider

import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.FloatInput


// TODO START HERE - find out where to use it - create some kind of DeviceFactory which will create RelayDevice to start with
class InputOutputProvider(
  private val hardwareInputOutputProvider: HardwareInputOutputProvider
) {

  fun getBinaryInput(source: Source, connectorConfiguration: ConnectorConfiguration): BinaryInput {
    // TODO what about other sources?
    return hardwareInputOutputProvider.getBinaryInput(connectorConfiguration)
  }

  fun getBinaryOutput(source: Source, connectorConfiguration: ConnectorConfiguration): BinaryOutput {
    // TODO what about other sources?
    return hardwareInputOutputProvider.getBinaryOutput(connectorConfiguration)
  }

  fun getFloatInput(source: Source, connectorConfiguration: ConnectorConfiguration): FloatInput {
    // TODO what about other sources?
    return hardwareInputOutputProvider.getFloatInput(connectorConfiguration)
  }
}

enum class Source {
  HARDWARE, MYSENSORS, ONEWIRE
}
