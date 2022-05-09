package com.mqgateway.core.io


import org.jetbrains.annotations.NotNull

class TestSerial implements Serial {

  List<SerialDataEventListener> eventListeners = []
  List<String> sentMessages = []

  @Override
  void open(@NotNull String portDescriptor, int baudRate) {
    // nothing to do
  }

  @Override
  void addListener(SerialDataEventListener serialDataEventListener) {
    eventListeners.add(serialDataEventListener)
  }

  @Override
  void write(String message) {
    sentMessages.add(message)
  }

  void simulateMessageReceived(String message) {
    eventListeners.forEach { it.dataReceived(new SerialDataEvent(message))}
  }

}
