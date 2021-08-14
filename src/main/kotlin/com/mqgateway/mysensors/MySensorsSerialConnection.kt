package com.mqgateway.mysensors

import com.mqgateway.core.hardware.MqSerial
import com.mqgateway.core.hardware.MqSerialDataEvent
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class MySensorsSerialConnection(private val serial: MqSerial, private val messageParser: MySensorMessageParser) {

  private val listeners: MutableMap<Int, MySensorsSerialListener> = mutableMapOf()
  private var initialized: Boolean = false

  fun init() {
    if (initialized) throw SerialConnectionAlreadyInitializedException()
    serial.addListener { event: MqSerialDataEvent ->
      val dataString = event.getAsciiString()
      dataString.split("\n")
        .filter { it.isNotBlank() }
        .map { messageString ->
          LOGGER.info { "Received Serial message: $messageString" }
          messageParser.parse(messageString)
        }.forEach { message ->
          listeners[message.nodeId]?.onMessageReceived(message)
        }
    }
    initialized = true
  }

  fun registerDeviceListener(nodeId: Int, listener: MySensorsSerialListener) {
    listeners[nodeId] = listener
  }

  fun publishMessage(message: Message) {
    LOGGER.info { "Sending message: $message" }
    val messageString = messageParser.serialize(message)
    serial.write(messageString)
  }
}

interface MySensorsSerialListener {
  fun onMessageReceived(message: Message)
}

class SerialConnectionAlreadyInitializedException : RuntimeException("Serial connection cannot be initialized twice")
