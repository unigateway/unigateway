package com.mqgateway.core.utils

import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.hardware.MqSerial
import com.mqgateway.core.hardware.MqSerialDataEvent
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private val LOGGER = KotlinLogging.logger {}

class SerialConnection(private val serial: MqSerial, private val maxWaitTimeMs: Long = 5000L) {

  private var deferredUntilMessageReceived = CompletableFuture<String>()

  private val listeners: ConcurrentLinkedQueue<SerialDataListener> = ConcurrentLinkedQueue()
  private var initialized: Boolean = false

  fun init() {
    if (initialized) throw SerialConnectionAlreadyInitializedException()
    serial.addListener { event: MqSerialDataEvent ->
      val message = event.getAsciiString()
      LOGGER.info { "Received Serial event with message: $message" }
      deferredUntilMessageReceived.complete(message)
    }
    initialized = true
  }

  fun askForData(listener: SerialDataListener) {
    if (!initialized) {
      throw SerialConnectionNotInitializedException()
    }
    listeners.offer(listener)
  }

  fun getDataForAllListeners() {
    while (listeners.peek() != null) {
      val listener = listeners.poll()
      val message = askForData(listener.id(), listener.askForDataPin())
      listener.onDataReceived(message)
    }
  }

  private fun askForData(id: String, askForDataPin: MqGpioPinDigitalOutput): String? {
    LOGGER.debug { "Device $id is asking for Serial data" }

    deferredUntilMessageReceived = CompletableFuture()
    askForDataPin.low()

    val receivedMessage: String? = try {
      deferredUntilMessageReceived.get(maxWaitTimeMs, TimeUnit.MILLISECONDS)
    } catch (exception: TimeoutException) {
      null
    }

    askForDataPin.high()
    if (receivedMessage != null) {
      LOGGER.info { "Data received on serial for device '$id': $receivedMessage" }
    }
    return receivedMessage
  }
}

interface SerialDataListener {
  fun id(): String
  fun askForDataPin(): MqGpioPinDigitalOutput
  fun onDataReceived(message: String?)
}

class SerialConnectionNotInitializedException : RuntimeException("Serial connection has to be initialized by calling init() before being used")
class SerialConnectionAlreadyInitializedException : RuntimeException("Serial connection cannot be initialized twice")
