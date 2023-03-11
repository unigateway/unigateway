package com.mqgateway.core.device.shutter

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.shutter.ShutterDevice.Companion.DOWN_BUTTON_REFERENCE_NAME
import com.mqgateway.core.device.shutter.ShutterDevice.Companion.UP_BUTTON_REFERENCE_NAME
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration

class ShutterDeviceFactory : DeviceFactory<ShutterDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.SHUTTER
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): ShutterDevice {
    val stopRelay = getReferenceDevice(deviceConfiguration, "stopRelay", devices) as RelayDevice
    val upDownRelay = getReferenceDevice(deviceConfiguration, "upDownRelay", devices) as RelayDevice
    val upButton = getOptionalReferenceDevice(deviceConfiguration, UP_BUTTON_REFERENCE_NAME, devices) as SwitchButtonDevice?
    val downButton = getOptionalReferenceDevice(deviceConfiguration, DOWN_BUTTON_REFERENCE_NAME, devices) as SwitchButtonDevice?
    return ShutterDevice(
      deviceConfiguration.id,
      deviceConfiguration.name,
      stopRelay,
      upDownRelay,
      deviceConfiguration.config.getValue("fullOpenTimeMs").toLong(),
      deviceConfiguration.config.getValue("fullCloseTimeMs").toLong(),
      upButton,
      downButton,
      deviceConfiguration.config
    )
  }
}
