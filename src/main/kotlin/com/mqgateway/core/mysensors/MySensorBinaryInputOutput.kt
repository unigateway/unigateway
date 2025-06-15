package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateChangeEvent
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.provider.MySensorsConnector

class MySensorBinaryInput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : BinaryInput {
  private val stateListener = LastBinaryStateListener()

  init {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToBinaryValueListener(stateListener, connector.type))
  }

  override fun addListener(listener: BinaryStateListener) {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToBinaryValueListener(listener, connector.type))
  }

  override fun getState(): BinaryState {
    return stateListener.state
  }
}

class MySensorBinaryOutput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : BinaryOutput {
  override fun setState(newState: BinaryState) {
    serialConnection.publishMessage(
      Message(connector.nodeId, connector.sensorId, Command.SET, false, connector.type, MySensorPayloadConverter.serializeBinary(newState)),
    )
  }
}

class LastBinaryStateListener : BinaryStateListener {
  var state: BinaryState = BinaryState.LOW

  override fun handle(event: BinaryStateChangeEvent) {
    this.state = event.newState()
  }
}
