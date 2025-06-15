package com.mqgateway.core.device.light

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration

class LightDeviceFactory : DeviceFactory<LightDevice> {
  override fun deviceType() = DeviceType.LIGHT

  override fun create(
    deviceConfiguration: DeviceConfiguration,
    devices: Set<Device>,
  ): LightDevice {
    val relay = getReferenceDevice(deviceConfiguration, "relay", devices) as RelayDevice
    val switches: List<SwitchButtonDevice> =
      deviceConfiguration.internalDevices
        .filterKeys { it.startsWith("switch") }
        .map { getReferenceDevice(deviceConfiguration, it.key, devices) as SwitchButtonDevice }

    return LightDevice(
      deviceConfiguration.id,
      deviceConfiguration.name,
      relay,
      switches,
    )
  }
}
