package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.provider.MySensorsConnector

class MySensorBinaryInput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : BinaryInput {

  override fun addListener(listener: BinaryStateListener) {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToBinaryValueListener(listener, connector.type))
  }

  override fun getState(): BinaryState {
    // todo we can have implementation here, that we add our listener and set last value. do we need it?
    throw UnsupportedOperationException("Reading value is not supported for MySensors")
  }
}

class MySensorBinaryOutput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : BinaryOutput {

  override fun setState(newState: BinaryState) {
    // todo what about command and ack?
    serialConnection.publishMessage(
      Message(connector.nodeId, connector.sensorId, Command.SET, false, connector.type, MySensorPayloadConverter.serializeBinary(newState))
    )
  }
}
