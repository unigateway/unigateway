package com.mqgateway.core.device

import com.mqgateway.core.device.unigateway.UniGatewayDevice

class DeviceRegistry(val devices: Set<Device>) {
  fun getById(deviceId: String): Device? = devices.find { it.id == deviceId }

  fun filterByType(type: DeviceType): List<Device> = devices.filter { it.type == type }

  fun getUniGatewayDevice(): UniGatewayDevice {
    val unigatewayDevices: List<Device> = filterByType(DeviceType.UNIGATEWAY)
    if (unigatewayDevices.size != 1) {
      throw IllegalStateException(
        "There is ${if (unigatewayDevices.size > 1) "more than one" else "no"} unigateway device configured in the registry",
      )
    }
    return unigatewayDevices.first() as UniGatewayDevice
  }

  fun addUpdateListener(updateListener: UpdateListener) {
    devices.forEach { it.addListener(updateListener) }
  }

  fun initializeDevices() {
    devices.forEach { it.init() }
  }
}
