package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfiguration

interface DeviceFactory<out T : Device> {

  fun deviceType(): DeviceType

  fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): T

  fun getReferenceDevice(deviceConfiguration: DeviceConfiguration, connectorName: String, devices: Set<Device>): Device {
    val referenceId = deviceConfiguration.internalDevices.getValue(connectorName).referenceId
    return devices
      .find { it.id == referenceId }
      ?: throw ReferenceDeviceNotFoundException(deviceConfiguration.id, connectorName, referenceId)
  }

  fun getOptionalReferenceDevice(deviceConfiguration: DeviceConfiguration, connectorName: String, devices: Set<Device>): Device? {
    val referenceId = deviceConfiguration.internalDevices[connectorName]?.referenceId ?: return null
    return devices.find { it.id == referenceId }
        ?: throw ReferenceDeviceNotFoundException(deviceConfiguration.id, connectorName, referenceId)
  }
}

class MissingConnectorInDeviceConfigurationException(
  deviceId: String,
  connectorName: String
) : Exception("Missing connector configuration ($connectorName) for device: $deviceId")

class ReferenceDeviceNotFoundException(
  deviceId: String,
  connectorName: String,
  referenceId: String
) : Exception("Reference device not found ($connectorName:$referenceId) for device: $deviceId")

class UnexpectedDeviceConfigurationException(
  deviceId: String,
  additionalMessage: String
) : Exception("Unexpected device configuration for device: $deviceId. $additionalMessage")
