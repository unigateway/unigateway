package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import javax.inject.Singleton

@Singleton
class ReferenceDeviceValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    TODO()
  }

  class IncorrectReferencedDevice(val referencingDevice: DeviceConfiguration, val referencedDeviceId: String) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Device: '${referencingDevice.id}' has reference to device '$referencedDeviceId', but no such device exists"
    }
  }
}
