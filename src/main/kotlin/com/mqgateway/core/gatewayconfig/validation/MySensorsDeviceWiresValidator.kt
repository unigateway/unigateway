package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.device.mysensors.MySensorsDevice.Companion.CONFIG_MY_SENSORS_NODE_ID
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.WireColor
import javax.inject.Singleton

@Singleton
class MySensorsDeviceWiresValidator : GatewayValidator {
  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val devices: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type in listOf(DeviceType.BME280, DeviceType.DHT22) || device.config.containsKey(CONFIG_MY_SENSORS_NODE_ID) }

    return devices
      .filter { device -> device.wires.toSet() != setOf(WireColor.BROWN, WireColor.BROWN_WHITE) }
      .map { WrongWiresConfigurationForMySensorsDevice(it) }
  }

  class WrongWiresConfigurationForMySensorsDevice(val device: DeviceConfig) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Following MySensors serial-based device should have BROWN and BROWN_WHITE wires configured: ${device.name}"
    }
  }
}
