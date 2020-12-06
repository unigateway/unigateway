package com.mqgateway.core.utils

import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.serial.Serial
import com.pi4j.io.serial.SerialDataEvent
import com.pi4j.io.serial.SerialDataEventListener
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private val LOGGER = KotlinLogging.logger {}

class SerialConnection(private val serial: Serial, private val maxWaitTimeMs: Long = 5000L) {

  private var deferredUntilMessageReceived = CompletableFuture<String>()

  private val listeners: MutableList<SerialDataListener> = mutableListOf()
  private var initialized: Boolean = false

  fun init() {
    if (initialized) throw SerialConnectionAlreadyInitializedException()
    serial.addListener(SerialDataEventListener { event: SerialDataEvent? ->
      val message = event!!.asciiString
      LOGGER.info { "Received Serial event with message: $message" }
      deferredUntilMessageReceived.complete(message)
    })
    initialized = true
  }

  fun askForData(listener: SerialDataListener) {
    if (!initialized) {
      throw SerialConnectionNotInitializedException()
    }
    listeners.add(listener)
  }

  @Scheduled(fixedDelay = "30s")
  fun getDataForAllListeners() {
    val listenersCopy = listeners.toMutableList()
    for (listener in listenersCopy) {
      val message = askForData(listener.id(), listener.askForDataPin())
      listener.onDataReceived(message)
    }
    listeners.removeAll(listenersCopy)
  }

  private fun askForData(id: String, askForDataPin: GpioPinDigitalOutput): String? {
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
  fun askForDataPin(): GpioPinDigitalOutput
  fun onDataReceived(message: String?)
}

class SerialConnectionNotInitializedException : RuntimeException("Serial connection has to be initialized by calling init() before being used")
class SerialConnectionAlreadyInitializedException : RuntimeException("Serial connection cannot be initialized twice")
