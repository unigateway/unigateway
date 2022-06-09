package com.mqgateway.core.mysensors

import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatValueListener
import com.mqgateway.core.io.provider.MySensorsConnector

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
