package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.DeviceConfig

class DeviceNameValidator: GatewayValidator {

  override fun validate(gateway: Gateway): List<ValidationFailureReason> {
    val devices = gateway.rooms.flatMap { it.points }.flatMap { it.devices }
    return devices.filter { it.name.length > 32 }.map { IllegalDeviceNameValue(it) }
  }

  class IllegalDeviceNameValue(private val device: DeviceConfig): ValidationFailureReason() {
    override fun getDescription(): String {
      return "Device with id '${device.id}' has illegal name=${device.name}. Device name can only consists of letter and number and be maximum 32 characters long."
    }
  }
}