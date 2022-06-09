package com.unigateway.core.io.provider

import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryOutput
import com.unigateway.core.io.FloatInput
import com.unigateway.core.io.FloatOutput
import com.unigateway.core.mysensors.MySensorBinaryInput
import com.unigateway.core.mysensors.MySensorBinaryOutput
import com.unigateway.core.mysensors.MySensorFloatInput
import com.unigateway.core.mysensors.MySensorFloatOutput
import com.unigateway.core.mysensors.MySensorsSerialConnection

interface MySensorsInputOutputProvider {
  fun getBinaryInput(connector: MySensorsConnector): BinaryInput
  fun getBinaryOutput(connector: MySensorsConnector): BinaryOutput
  fun getFloatInput(connector: MySensorsConnector): FloatInput
  fun getFloatOutput(connector: MySensorsConnector): FloatOutput
}

class DefaultMySensorsInputOutputProvider(private val serialConnection: MySensorsSerialConnection) : MySensorsInputOutputProvider {

  override fun getBinaryInput(connector: MySensorsConnector): BinaryInput {
    return MySensorBinaryInput(serialConnection, connector)
  }

  override fun getBinaryOutput(connector: MySensorsConnector): BinaryOutput {
    return MySensorBinaryOutput(serialConnection, connector)
  }

  override fun getFloatInput(connector: MySensorsConnector): FloatInput {
    return MySensorFloatInput(serialConnection, connector)
  }

  override fun getFloatOutput(connector: MySensorsConnector): FloatOutput {
    return MySensorFloatOutput(serialConnection, connector)
  }
}
