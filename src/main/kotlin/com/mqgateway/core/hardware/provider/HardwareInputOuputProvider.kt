package com.mqgateway.core.hardware.provider

import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.FloatInput

interface HardwareInputOutputProvider<T : ConnectorConfiguration> {
  fun getBinaryInput(connectorConfiguration: T): BinaryInput
  fun getBinaryOutput(connectorConfiguration: T): BinaryOutput
  fun getFloatInput(connectorConfiguration: T): FloatInput
}

interface ConnectorConfiguration
