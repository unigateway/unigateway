package com.mqgateway.core.mysensors

import com.mqgateway.core.io.Serial
import com.mqgateway.core.io.SerialDataEvent
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class MySensorsSerialConnection(private val serial: Serial, private val messageParser: MySensorMessageSerializer) {

  private val listeners: MutableMap<Int, MutableMap<Int, MutableList<MySensorsSerialListener>>> = mutableMapOf()

  init {
    serial.addListener { event: SerialDataEvent ->
      event.data.split("\n")
        .filter { it.isNotBlank() }
        .map { messageString ->
          LOGGER.info { "Received Serial message: $messageString" }
          messageParser.deserialize(messageString)
        }.forEach { message ->
          listeners[message.nodeId]?.get(message.childSensorId)
            ?.forEach { it.onMessageReceived(message) }
        }
    }
  }

  fun registerDeviceListener(nodeId: Int, sensorId: Int, listener: MySensorsSerialListener) {
    listeners
      .getOrPut(nodeId) { mutableMapOf() }
      .getOrPut(sensorId) { mutableListOf() }
      .add(listener)
  }

  fun publishMessage(message: Message) {
    LOGGER.info { "Sending message: $message" }
    serial.write(messageParser.serialize(message))
  }
}
