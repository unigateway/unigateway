package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class ReferenceDeviceValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val referenceDevices: List<DeviceConfiguration> = gatewayConfiguration.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type == DeviceType.REFERENCE }

    return referenceDevices
      .filter { referenceDevice ->
        gatewayConfiguration.allDevices().none { device -> device.id == referenceDevice.referencedDeviceId }
      }.map { IncorrectReferencedDevice(it, it.referencedDeviceId!!) }
  }

  class IncorrectReferencedDevice(val referencingDevice: DeviceConfiguration, val referencedDeviceId: String) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Device: '${referencingDevice.id}' has reference to device '$referencedDeviceId', but no such device exists"
    }
  }
}
