package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class ShutterAdditionalConfigValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    TODO()
  }

  class NonRelayShutterInternalDevice(val device: DeviceConfiguration) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect internal device for shutter: '${device.name}'. It should be RELAY."
    }
  }
}
