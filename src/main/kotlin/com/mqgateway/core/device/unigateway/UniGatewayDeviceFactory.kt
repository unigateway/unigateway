package com.mqgateway.core.device.unigateway

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.utils.SystemInfoProvider
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
