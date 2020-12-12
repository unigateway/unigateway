package com.mqgateway.core.hardware

interface MqSerial {
  fun open(devicePath: String, baudRate: Int)
  fun addListener(serialDataEventListener: MqSerialDataEventListener)
}

fun interface MqSerialDataEventListener {
  fun dataReceived(event: MqSerialDataEvent)
}

interface MqSerialDataEvent {
  fun getAsciiString(): String
}
