package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType

interface DeviceFactory<out T : Device> {

  fun deviceType(): DeviceType

  fun create(deviceConfiguration: DeviceConfiguration): T
}

class MissingConnectorInDeviceConfigurationException(
  deviceId: String,
  connectorName: String
) : Exception("Missing connector configuration ($connectorName) for device: $deviceId")
