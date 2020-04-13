package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.Gateway

class UniqueDeviceIdsValidator : GatewayValidator {

  override fun validate(gateway: Gateway): List<ValidationFailureReason> {
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
