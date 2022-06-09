package com.unigateway.core.io.provider

import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryOutput
import com.unigateway.core.io.FloatInput
import com.unigateway.core.io.FloatOutput

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
  "MySensors is disabled in gateway configuration. If you want to use MySensors, you need to enable it in gateway.system.mysensors.enabled"
)
