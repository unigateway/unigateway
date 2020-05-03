package com.mqgateway.core.onewire.device

import mu.KotlinLogging
import java.nio.file.Paths

private val LOGGER = KotlinLogging.logger {}

class DS18B20(oneWireAddress: String) : OneWireBusDevice(oneWireAddress) {

  private val valueRegex = Regex("(?s).*crc=[0-9a-f]+ (?<success>[A-Z]+).*t=(?<temp>-?[0-9]+)")
  override fun deviceValueFileName() = DEFAULT_DEVICE_VALUE_FILE

  override fun readValue(masterDirPath: String): String? {
    val valueFilePath = Paths.get(masterDirPath, address, deviceValueFileName())
    val deviceValueFile = valueFilePath.toFile()
    if (!deviceValueFile.exists()) {
      LOGGER.error { "Device '$address' is not connected. It has not been found in sysfs structure. Check connection." }
      return null
    }

    val fileText = deviceValueFile.readText()
    return if (valueRegex.find(fileText)?.groups?.get("success")?.value != "YES") {
      LOGGER.warn { "Cannot read value. CRC check failed for device '$address'" }
      null
    } else {
      valueRegex.find(fileText)?.groups?.get("temp")?.value
    }
  }
}
