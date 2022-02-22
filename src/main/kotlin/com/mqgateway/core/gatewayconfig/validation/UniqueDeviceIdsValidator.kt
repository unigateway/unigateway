package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.Gateway
import jakarta.inject.Singleton

@Singleton
class UniqueDeviceIdsValidator : GatewayValidator {

  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val devices: List<DeviceConfig> = gateway.rooms.flatMap { room -> room.points }.flatMap { point -> point.devices }
    return devices.groupBy { device -> device.id }.filter { it.value.size > 1 }.values.toList().map { DuplicatedDeviceIds(it) }
  }

  class DuplicatedDeviceIds(private val duplicates: List<DeviceConfig>) : ValidationFailureReason() {
    override fun getDescription(): String {
      val duplicatedId = duplicates.first().id
      val duplicatedDevicesNames = duplicates.map { it.name }
      return "Devices with following names has duplicated id (=$duplicatedId): $duplicatedDevicesNames"
    }
  }
}
