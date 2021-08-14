package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.Gateway
import javax.inject.Singleton

@Singleton
class WireUsageValidator : GatewayValidator {
  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val deviceConfigs: List<List<DeviceConfig>> = gateway.rooms.flatMap { room -> room.points }.map { it.devices }

    return deviceConfigs
      .map { devices ->
        devices
          .filter { device -> !device.config.containsKey("mySensorsNodeId") }
          .filter { device ->
            val otherDevices = devices.filterNot { otherDevice -> otherDevice.id == device.id }
            val otherDevicesWires = otherDevices.flatMap { otherDevice -> otherDevice.wires }
            device.wires.intersect(otherDevicesWires).isNotEmpty()
          }
      }
      .filter { it.isNotEmpty() }
      .map {
        SameWireUsedInManyDevices(it)
      }
  }

  class SameWireUsedInManyDevices(val devices: List<DeviceConfig>) : ValidationFailureReason() {

    override fun getDescription(): String {
      val duplicatedWiresNames = devices.map { it.wires }.let { it[0].intersect(it[1]) }.map { it.name }
      val devicesNames = devices.joinToString { it.name }
      return "Following devices are set up to use same wires ($duplicatedWiresNames): $devicesNames"
    }
  }
}
