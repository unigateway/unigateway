package com.mqgateway.core.onewire

import com.mqgateway.core.onewire.device.DS18B20
import com.mqgateway.core.onewire.device.OneWireBusDevice
import kotlin.concurrent.thread

object OneWireBusDeviceFactory {
  fun create(oneWireAddress: String): OneWireBusDevice {
    val familyCode = oneWireAddress.substring(0, 2).toInt(16)
    val deviceType = OneWireBusDeviceType.values().find { it.familyCode == familyCode }
    return when (deviceType) {
      OneWireBusDeviceType.DS18B20 -> DS18B20(oneWireAddress)
      OneWireBusDeviceType.DS18S20 -> TODO()
      OneWireBusDeviceType.DS1822 -> TODO()
      OneWireBusDeviceType.DS1825 -> TODO()
      OneWireBusDeviceType.DS28EA00 -> TODO()
      OneWireBusDeviceType.DS2405 -> TODO()
      OneWireBusDeviceType.DS2406 -> TODO()
      OneWireBusDeviceType.DS2408 -> TODO()
      OneWireBusDeviceType.DS2413 -> TODO()
      OneWireBusDeviceType.DS2423 -> TODO()
      OneWireBusDeviceType.DS2431 -> TODO()
      OneWireBusDeviceType.DS2433 -> TODO()
      OneWireBusDeviceType.DS2502 -> TODO()
      OneWireBusDeviceType.DS2505 -> TODO()
      OneWireBusDeviceType.DS2506 -> TODO()
      null -> TODO()
    }
  }
}

open class OneWireBus(private val masterDirPath: String, private val readIntervalInMs: Long = 5000) {

  constructor() : this(DEFAULT_DIR)

  private val devices: MutableMap<String, OneWireBusDevice> = mutableMapOf()

  fun registerDevice(device: OneWireBusDevice): OneWireBusDevice {
    return devices.getOrPut(device.address, { device })
  }

  fun start() {
    thread(isDaemon = true, name = "OneWireBusDevices") {
      while (true) {
        devices.values.forEach { it.checkValue(masterDirPath) }
        Thread.sleep(readIntervalInMs)
      }
    }
  }

  companion object {
    const val DEFAULT_DIR = "/sys/bus/w1/devices"
  }
}

class OneWireBusValueReceivedEvent(val newValue: String)

class OneWireBusDeviceConnectionEvent(val type: ConnectionEventType) {

  enum class ConnectionEventType {
    CONNECTED, DISCONNECTED
  }
}

enum class OneWireBusDeviceType(val familyCode: Int) {
  DS18B20(0x28), DS18S20(0x10), DS1822(0x22), DS1825(0x3B), DS28EA00(0x42),
  DS2405(0x05), DS2406(0x12), DS2408(0x29), DS2413(0x3A), DS2423(0x1D), DS2431(0x2D), DS2433(0x23),
  DS2502(0x09), DS2505(0x0B), DS2506(0x0F)
}
