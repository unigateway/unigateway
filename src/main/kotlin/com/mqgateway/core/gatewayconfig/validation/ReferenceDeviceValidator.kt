package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import jakarta.inject.Singleton

@Singleton
class ReferenceDeviceValidator : GatewayValidator {
  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val referenceDevices: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type == DeviceType.REFERENCE }

    return referenceDevices
      .filter { referenceDevice ->
        gateway.allDevices().none { device -> device.id == referenceDevice.referencedDeviceId }
      }.map { IncorrectReferencedDevice(it, it.referencedDeviceId!!) }
  }

  class IncorrectReferencedDevice(val referencingDevice: DeviceConfig, val referencedDeviceId: String) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Device: '${referencingDevice.id}' has reference to device '$referencedDeviceId', but no such device exists"
    }
  }
}
