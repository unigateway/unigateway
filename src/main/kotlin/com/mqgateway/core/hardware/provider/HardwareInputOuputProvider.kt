package com.mqgateway.core.hardware.provider

import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.FloatInput

interface HardwareInputOutputProvider {
  fun getBinaryInput(connectorConfiguration: ConnectorConfiguration): BinaryInput
  fun getBinaryOutput(connectorConfiguration: ConnectorConfiguration): BinaryOutput
  fun getFloatInput(connectorConfiguration: ConnectorConfiguration): FloatInput
}

interface ConnectorConfiguration
