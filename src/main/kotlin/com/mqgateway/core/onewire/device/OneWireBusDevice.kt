package com.mqgateway.core.onewire.device

import com.mqgateway.core.onewire.OneWireBus
import com.mqgateway.core.onewire.OneWireBusDeviceConnectionEvent
import com.mqgateway.core.onewire.OneWireBusValueReceivedEvent
import mu.KotlinLogging
import java.nio.file.Paths

private val LOGGER = KotlinLogging.logger {}

abstract class OneWireBusDevice(val address: String) {

  private var connected = false
  private var lastValue = ""
  private val valueReceivedListeners: MutableList<(OneWireBusValueReceivedEvent) -> Unit> = mutableListOf()
  private val deviceConnectionListeners: MutableList<(OneWireBusDeviceConnectionEvent) -> Unit> = mutableListOf()

  private fun notifyConnected() {
    this.connected = true
    deviceConnectionListeners.forEach { it(OneWireBusDeviceConnectionEvent(OneWireBusDeviceConnectionEvent.ConnectionEventType.CONNECTED)) }
  }

  private fun notifyDisconnected() {
    this.connected = false
    deviceConnectionListeners.forEach { it(OneWireBusDeviceConnectionEvent(OneWireBusDeviceConnectionEvent.ConnectionEventType.DISCONNECTED)) }
  }

  fun isAvailable(): Boolean {
    val deviceFilePath = Paths.get(OneWireBus.DEFAULT_DIR, address)
    return deviceFilePath.toFile().exists()
  }

  fun addValueReceivedListener(listener: (OneWireBusValueReceivedEvent) -> Unit) {
    valueReceivedListeners.add(listener)
  }

  fun addDeviceConnectionListener(listener: (OneWireBusDeviceConnectionEvent) -> Unit) {
    deviceConnectionListeners.add(listener)
  }

  fun checkValue(masterDirPath: String) {
    val value = readValue(masterDirPath)
    if (value == null) {
      LOGGER.error { "Failure when reading value from file for device '$address'" }
      if (connected) {
        notifyDisconnected()
      }
      return
    }

    if (lastValue != value) {
      lastValue = value
      valueReceivedListeners.forEach { notify -> notify(OneWireBusValueReceivedEvent(value)) }
    }

    if (!connected) {
      notifyConnected()
    }
  }

  internal abstract fun deviceValueFileName(): String
  abstract fun readValue(masterDirPath: String): String?

  companion object {
    const val DEFAULT_DEVICE_VALUE_FILE = "w1_slave"
  }
}
