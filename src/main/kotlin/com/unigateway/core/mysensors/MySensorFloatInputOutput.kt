package com.unigateway.core.mysensors

import com.unigateway.core.io.FloatInput
import com.unigateway.core.io.FloatOutput
import com.unigateway.core.io.FloatValueListener
import com.unigateway.core.io.provider.MySensorsConnector

class MySensorFloatInput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : FloatInput {
  override fun addListener(listener: FloatValueListener) {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToFloatValueListener(listener, connector.type))
  }

  override fun getValue(): Float {
    return 0f
  }
}

class MySensorFloatOutput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : FloatOutput {
  override fun setValue(newValue: Float) {
    serialConnection.publishMessage(
      Message(connector.nodeId, connector.sensorId, Command.SET, false, connector.type, MySensorPayloadConverter.serializeFloat(newValue))
    )
  }
}
