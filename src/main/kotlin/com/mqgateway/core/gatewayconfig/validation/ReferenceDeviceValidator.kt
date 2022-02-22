package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class ReferenceDeviceValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val devicesWithInternalDevices: List<DeviceConfiguration> = gatewayConfiguration.devices
      .filter { device -> device.internalDevices.isNotEmpty() }

    return devicesWithInternalDevices
      .filter { deviceWithInternalDevice ->
        deviceWithInternalDevice.internalDevices.any { reference ->
          val (_, internalDevice) = reference
          !gatewayConfiguration.devices.map { it.id }.contains(internalDevice.referenceId)
        }
      }.map { IncorrectReferencedDevice(it) }
  }

  class IncorrectReferencedDevice(val referencingDevice: DeviceConfiguration) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Device: '${referencingDevice.id}' has internal device which is not configured"
    }
  }
}
