package com.mqgateway.core.device.factory

import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType

// todo how to organize packages in core.device?
//  - core.device.[device type] ?

interface DeviceFactory<out T : Device> {
  fun deviceType(): DeviceType
  // todo probably also created devices as an argument (or device registry)
  fun create(deviceConfiguration: DeviceConfiguration): T
}
