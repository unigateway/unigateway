package com.mqgateway.core.hardware.pi4j

import com.mqgateway.core.hardware.MqSerial
import com.mqgateway.core.hardware.MqSerialDataEvent
import com.mqgateway.core.hardware.MqSerialDataEventListener
import com.pi4j.io.serial.Baud
import com.pi4j.io.serial.Serial
import com.pi4j.io.serial.SerialConfig
import com.pi4j.io.serial.SerialDataEvent
import com.pi4j.io.serial.SerialDataEventListener

class Pi4JSerialDataEvent(private val serialDataEvent: SerialDataEvent) : MqSerialDataEvent {
  override fun getAsciiString(): String = serialDataEvent.asciiString
}

class Pi4JSerial(private val serial: Serial) : MqSerial {

  override fun open(devicePath: String, baudRate: Int) {
    serial.open(
      SerialConfig()
        .device(devicePath)
        .baud(Baud.getInstance(baudRate))
    )
  }

  override fun addListener(serialDataEventListener: MqSerialDataEventListener) {
    serial.addListener(
      SerialDataEventListener { event ->
        serialDataEventListener.dataReceived(Pi4JSerialDataEvent(event))
      }
    )
  }

  override fun write(message: String) {
    serial.write(message)
  }
}
