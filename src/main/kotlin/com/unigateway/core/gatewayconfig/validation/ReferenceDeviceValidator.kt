package com.unigateway.core.gatewayconfig.validation

import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.InternalDeviceConfiguration

class ReferenceDeviceValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
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
