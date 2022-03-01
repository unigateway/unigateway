package com.mqgateway.core.device.shutter

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType

class ShutterDeviceFactory : DeviceFactory<ShutterDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.SHUTTER
  }

  override fun create(deviceConfiguration: DeviceConfiguration): ShutterDevice {
    TODO()
    // val stopRelayDevice = create(deviceConfig.internalDevices.getValue("stopRelay"), gateway) as RelayDevice
    // val upDownRelayDevice = create(deviceConfig.internalDevices.getValue("upDownRelay"), gateway) as RelayDevice
    // ShutterDevice(
    //   deviceConfig.id,
    //   stopRelayDevice,
    //   upDownRelayDevice,
    //   deviceConfig.config.getValue("fullOpenTimeMs").toLong(),
    //   deviceConfig.config.getValue("fullCloseTimeMs").toLong()
    // )
  }
}
