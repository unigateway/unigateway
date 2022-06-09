package com.unigateway.core.device.shutter

import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceFactory
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.relay.RelayDevice
import com.unigateway.core.gatewayconfig.DeviceConfiguration

class ShutterDeviceFactory : DeviceFactory<ShutterDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.SHUTTER
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): ShutterDevice {
    val stopRelay = getReferenceDevice(deviceConfiguration, "stopRelay", devices) as RelayDevice
    val upDownRelay = getReferenceDevice(deviceConfiguration, "upDownRelay", devices) as RelayDevice
    return ShutterDevice(
      deviceConfiguration.id,
      deviceConfiguration.name,
      stopRelay,
      upDownRelay,
      deviceConfiguration.config.getValue("fullOpenTimeMs").toLong(),
      deviceConfiguration.config.getValue("fullCloseTimeMs").toLong()
    )
  }
}
