package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import jakarta.inject.Singleton

@Singleton // todo remove?
class ReferenceDeviceValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    return gatewayConfiguration.devices
      .flatMap { device ->
        device.internalDevices.filter { (_: String, internalDevice: InternalDeviceConfiguration) ->
          !gatewayConfiguration.devices.map { it.id }.contains(internalDevice.referenceId)
        }.map { IncorrectReferencedDevice(device, it.value.referenceId) }
      }
  }

  class IncorrectReferencedDevice(val referencingDevice: DeviceConfiguration, val referencedDeviceId: String) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Device: '${referencingDevice.id}' has reference to device '$referencedDeviceId', but no such device exists"
    }
  }
}
