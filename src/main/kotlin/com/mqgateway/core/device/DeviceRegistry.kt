package com.mqgateway.core.device

class DeviceRegistry(private val devices: Set<Device>) {

  fun getById(deviceId: String): Device? = devices.find { it.id == deviceId }

  fun addUpdateListener(updateListener: UpdateListener) {
    devices.forEach { it.addListener(updateListener) }
  }

  fun initailizeDevices() {
    devices.forEach { it.init() }
  }

}