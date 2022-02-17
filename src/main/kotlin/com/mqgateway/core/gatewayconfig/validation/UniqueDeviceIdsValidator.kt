package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import javax.inject.Singleton

@Singleton
class UniqueDeviceIdsValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    TODO()
  }

  class DuplicatedDeviceIds(private val duplicates: List<DeviceConfiguration>) : ValidationFailureReason() {
    override fun getDescription(): String {
      val duplicatedId = duplicates.first().id
      val duplicatedDevicesNames = duplicates.map { it.name }
      return "Devices with following names has duplicated id (=$duplicatedId): $duplicatedDevicesNames"
    }
  }
}
