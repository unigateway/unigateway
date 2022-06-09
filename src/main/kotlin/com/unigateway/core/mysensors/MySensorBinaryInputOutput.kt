package com.unigateway.core.mysensors

import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryOutput
import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.BinaryStateListener
import com.unigateway.core.io.provider.MySensorsConnector

class MySensorBinaryInput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : BinaryInput {

  override fun addListener(listener: BinaryStateListener) {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToBinaryValueListener(listener, connector.type))
  }

  override fun getState(): BinaryState {
    return BinaryState.LOW
  }
}

class MySensorBinaryOutput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : BinaryOutput {

  override fun setState(newState: BinaryState) {
    serialConnection.publishMessage(
      Message(connector.nodeId, connector.sensorId, Command.SET, false, connector.type, MySensorPayloadConverter.serializeBinary(newState))
    )
  }
}
