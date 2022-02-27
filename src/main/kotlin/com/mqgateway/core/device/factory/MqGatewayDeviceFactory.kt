package com.mqgateway.core.device.factory

import com.mqgateway.core.device.MqGatewayDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.utils.SystemInfoProvider
import java.time.Duration

class MqGatewayDeviceFactory(
  private val systemInfoProvider: SystemInfoProvider
) : DeviceFactory<MqGatewayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.MQGATEWAY
  }

  override fun create(deviceConfiguration: DeviceConfiguration): MqGatewayDevice {
    return MqGatewayDevice(deviceConfiguration.id, Duration.ofSeconds(30), systemInfoProvider)
  }
}
