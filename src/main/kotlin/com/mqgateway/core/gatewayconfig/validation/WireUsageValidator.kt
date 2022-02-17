package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import javax.inject.Singleton

@Singleton
class WireUsageValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    TODO()
  }

  class SameWireUsedInManyDevices(val devices: List<DeviceConfiguration>) : ValidationFailureReason() {

    override fun getDescription(): String {
      TODO()
    }
  }
}
