package com.mqgateway.core.io

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean

private val LOGGER = KotlinLogging.logger {}

class JCommSerial : Serial {

  private var serialPort: SerialPort? = null
  private val isOpen: AtomicBoolean = AtomicBoolean(false)
  private val dataReceivedEventListener = DataReceivedEventListener()

  override fun open(portDescriptor: String, baudRate: Int) {
    if (isOpen.getAndSet(true)) {
      throw IllegalStateException("Serial port is already open")
    }

    LOGGER.info { "Opening port: $portDescriptor. Baud rate: $baudRate." }

    serialPort = SerialPort.getCommPort(portDescriptor)
    serialPort?.baudRate = baudRate
    serialPort?.openPort()
    serialPort?.addDataListener(dataReceivedEventListener)
  }

  override fun addListener(serialDataEventListener: SerialDataEventListener) {
    dataReceivedEventListener.addListener(serialDataEventListener)
  }

  override fun write(message: String) {
    val bytes = message.toByteArray()
    serialPort!!.writeBytes(bytes, bytes.size.toLong())
  }
}

private class DataReceivedEventListener : SerialPortDataListener {

  private val listeners = mutableListOf<SerialDataEventListener>()

  override fun getListeningEvents(): Int {
    return SerialPort.LISTENING_EVENT_DATA_RECEIVED
  }

  override fun serialEvent(event: SerialPortEvent) {
    val data = String(event.receivedData)
    LOGGER.info { "Handling serial event: $event, serial data: $data." }

    if (listeners.isEmpty()) {
      LOGGER.warn { "No registered listeners for serial port." }
    }

    try {
      listeners.forEach { it.dataReceived(SerialDataEvent(data)) }
    } catch (e: Exception) {
      LOGGER.error { "One of listeners thrown an error: $e" }
    }
  }

  fun addListener(listener: SerialDataEventListener) {
    listeners.add(listener)
  }
}
