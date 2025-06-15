package com.mqgateway.core.io

interface Serial {
  fun open(
    portDescriptor: String,
    baudRate: Int,
  )

  fun addListener(serialDataEventListener: SerialDataEventListener)

  fun write(message: String)
}

fun interface SerialDataEventListener {
  fun dataReceived(event: SerialDataEvent)
}

data class SerialDataEvent(val data: String)
