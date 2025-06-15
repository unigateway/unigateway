package com.mqgateway.core.io.provider

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput

class DisabledMySensorsInputOutputProvider : MySensorsInputOutputProvider {
  override fun getBinaryInput(connector: MySensorsConnector): BinaryInput {
    throw MySensorsIsDisabledException()
  }

  override fun getBinaryOutput(connector: MySensorsConnector): BinaryOutput {
    throw MySensorsIsDisabledException()
  }

  override fun getFloatInput(connector: MySensorsConnector): FloatInput {
    throw MySensorsIsDisabledException()
  }

  override fun getFloatOutput(connector: MySensorsConnector): FloatOutput {
    throw MySensorsIsDisabledException()
  }
}

class MySensorsIsDisabledException : Exception(
  "MySensors is disabled in gateway configuration. If you want to use MySensors, you need to enable it in gateway.system.mysensors.enabled",
)
