package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.device.OneWireDevice
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway

class OneWireDeviceAddressValidator : GatewayValidator {

  override fun validate(gateway: Gateway): List<ValidationFailureReason> {
    val deviceConfigsMissingAddress: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { it.devices }
      .filter { it.type == DeviceType.DS18B20 || it.type == DeviceType.BME280 }
      .filter { it.config?.containsKey(OneWireDevice.CONFIG_ONE_WIRE_ADDRESS_KEY) == false }

    return deviceConfigsMissingAddress.map { OneWireDeviceAddressMissing(it) }
  }

  class OneWireDeviceAddressMissing(val device: DeviceConfig) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Following 1-wire device misses oneWireAddress in config: ${device.id}"
    }
  }
}
