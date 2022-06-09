package com.unigateway.core.mysensors

import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.BinaryStateChangeEvent
import com.unigateway.core.io.BinaryStateListener
import com.unigateway.core.io.FloatValueChangeEvent
import com.unigateway.core.io.FloatValueListener
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

interface MySensorsSerialListener {
  fun onMessageReceived(message: Message)
}

class MySensorMessageToFloatValueListener(private val floatListener: FloatValueListener, private val type: Type) : MySensorsSerialListener {

  override fun onMessageReceived(message: Message) {
    if (message.type == type) {
      floatListener.handle(MySensorFloatValueChangeEvent(message))
    } else {
      LOGGER.warn { "Message received, but has incorrect type: $message" }
    }
  }
}

class MySensorFloatValueChangeEvent(private val message: Message) : FloatValueChangeEvent {

  override fun newValue(): Float {
    return MySensorPayloadConverter.parseFloat(message.payload)
  }
}

class MySensorMessageToBinaryValueListener(private val binaryListener: BinaryStateListener, private val type: Type) : MySensorsSerialListener {

  override fun onMessageReceived(message: Message) {
    if (message.type == type) {
      binaryListener.handle(MySensorBinaryValueChangeEvent(message))
    }
  }
}

class MySensorBinaryValueChangeEvent(private val message: Message) : BinaryStateChangeEvent {
  override fun newState(): BinaryState {
    return MySensorPayloadConverter.parseBinary(message.payload)
  }
}
