package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
data class Gateway(
  val configVersion: String,
  val name: String,
  val mqttHostname: String,
  val rooms: List<Room>
) {
  fun allDevices(): List<DeviceConfig> = rooms.flatMap { it.points }.flatMap { it.devices }
  fun deviceById(id: String): DeviceConfig = allDevices().find { it.id == id } ?: throw UnknownDeviceIdException(id)
  fun portNumberByDeviceId(deviceId: String): Int {
    return rooms.flatMap { it.points }
      .find { it.devices.any { device -> device.id == deviceId } }?.portNumber ?: throw UnknownDeviceIdException(deviceId)
  }

  class UnknownDeviceIdException(deviceId: String) : RuntimeException("Device with id='$deviceId' not found")
}
