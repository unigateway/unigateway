package com.mqgateway.core.io.provider

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.mysensors.MySensorBinaryInput
import com.mqgateway.core.mysensors.MySensorBinaryOutput
import com.mqgateway.core.mysensors.MySensorFloatInput
import com.mqgateway.core.mysensors.MySensorFloatOutput
import com.mqgateway.core.mysensors.MySensorsSerialConnection

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
    // todo validate types that can have float
    return MySensorFloatInput(serialConnection, connector)
  }

  override fun getFloatOutput(connector: MySensorsConnector): FloatOutput {
    return MySensorFloatOutput(serialConnection, connector)
  }
}
