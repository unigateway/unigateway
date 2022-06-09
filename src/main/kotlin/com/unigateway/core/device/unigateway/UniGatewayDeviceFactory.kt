package com.unigateway.core.device.unigateway

import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceFactory
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.utils.SystemInfoProvider
import java.time.Duration

class UniGatewayDeviceFactory(
  private val systemInfoProvider: SystemInfoProvider
) : DeviceFactory<UniGatewayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.UNIGATEWAY
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): UniGatewayDevice {
    return UniGatewayDevice(
      deviceConfiguration.id, deviceConfiguration.name, Duration.ofSeconds(30), systemInfoProvider,
      deviceConfiguration.config
    )
  }
}
