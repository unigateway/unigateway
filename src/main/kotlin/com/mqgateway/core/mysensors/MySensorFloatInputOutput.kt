package com.mqgateway.core.mysensors

import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.FloatValueChangeEvent
import com.mqgateway.core.io.FloatValueListener
import com.mqgateway.core.io.provider.MySensorsConnector

class MySensorFloatInput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : FloatInput {

  private val valueListener = LastFloatValueListener()

  init {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToFloatValueListener(valueListener, connector.type))
  }

  override fun addListener(listener: FloatValueListener) {
    serialConnection.registerDeviceListener(connector.nodeId, connector.sensorId, MySensorMessageToFloatValueListener(listener, connector.type))
  }

  override fun getValue(): Float {
    return valueListener.value
  }
}

class MySensorFloatOutput(private val serialConnection: MySensorsSerialConnection, private val connector: MySensorsConnector) : FloatOutput {
  override fun setValue(newValue: Float) {
    serialConnection.publishMessage(
      Message(connector.nodeId, connector.sensorId, Command.SET, false, connector.type, MySensorPayloadConverter.serializeFloat(newValue))
    )
  }
}

class LastFloatValueListener : FloatValueListener {

  var value: Float = 0f

  override fun handle(event: FloatValueChangeEvent) {
    value = event.newValue()
  }
}
