package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateChangeEvent
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.FloatValueChangeEvent
import com.mqgateway.core.io.FloatValueListener
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
