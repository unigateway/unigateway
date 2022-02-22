package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class UniqueDeviceIdsValidator : GatewayValidator {

  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    return gatewayConfiguration.devices.groupBy { device -> device.id }.filter { it.value.size > 1 }.values.toList().map { DuplicatedDeviceIds(it) }
  }

  class DuplicatedDeviceIds(private val duplicates: List<DeviceConfiguration>) : ValidationFailureReason() {
    override fun getDescription(): String {
      val duplicatedId = duplicates.first().id
      val duplicatedDevicesNames = duplicates.map { it.name }
      return "Devices with following names has duplicated id (=$duplicatedId): $duplicatedDevicesNames"
    }
  }
}
