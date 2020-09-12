package com.mqgateway.core.utils

import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.serial.Serial
import com.pi4j.io.serial.SerialDataEvent
import com.pi4j.io.serial.SerialDataEventListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

@ObsoleteCoroutinesApi
class SerialConnection(private val serial: Serial, private val maxWaitTimeMs: Long = 5000L) {

  @Volatile
  private var deferredUntilMessageReceived = CompletableDeferred<String>()

  private val mutex = Mutex(false)

  fun init() {
    serial.addListener(SerialDataEventListener { event: SerialDataEvent? ->
      val message = event!!.asciiString
      LOGGER.info { "Received Serial event with message: $message" }
      deferredUntilMessageReceived.complete(message)
    })
  }

  suspend fun askForData(id: String, askForDataPin: GpioPinDigitalOutput): String? {
    mutex.withLock {
      LOGGER.debug { "Device $id is asking for Serial data" }

      deferredUntilMessageReceived = CompletableDeferred()
      askForDataPin.low()

      val receivedMessage = withTimeoutOrNull(maxWaitTimeMs) {
        deferredUntilMessageReceived.await()
      }
      askForDataPin.high()
      if (receivedMessage != null) {
        LOGGER.info { "Data received on serial for device '$id': $receivedMessage" }
      }
      return receivedMessage
    }
  }
}
