package com.unigateway.core.mysensors

import com.unigateway.core.io.Serial
import com.unigateway.core.io.SerialDataEvent
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class MySensorsSerialConnection(private val serial: Serial, private val messageParser: MySensorMessageSerializer) {

  private val listeners: MutableMap<Int, MutableMap<Int, MySensorsSerialListener>> = mutableMapOf()

  init {
    serial.addListener { event: SerialDataEvent ->
      event.data.split("\n")
        .filter { it.isNotBlank() }
        .map { messageString ->
          LOGGER.info { "Received Serial message: $messageString" }
          messageParser.deserialize(messageString)
        }.forEach { message ->
          listeners[message.nodeId]?.get(message.childSensorId)?.onMessageReceived(message)
        }
    }
  }

  fun registerDeviceListener(nodeId: Int, sensorId: Int, listener: MySensorsSerialListener) {
    listeners
      .getOrPut(nodeId) { mutableMapOf() }
      .putIfAbsent(sensorId, listener)
  }

  fun publishMessage(message: Message) {
    LOGGER.info { "Sending message: $message" }
    serial.write(messageParser.serialize(message))
  }
}
