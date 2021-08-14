package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.device.mysensors.MySensorsDevice.Companion.CONFIG_MY_SENSORS_NODE_ID
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import javax.inject.Singleton

@Singleton
class MySensorsDeviceAdditionalConfigValidator : GatewayValidator {
  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val devices: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type in listOf(DeviceType.BME280, DeviceType.DHT22) || device.config.containsKey(CONFIG_MY_SENSORS_NODE_ID) }

    val missingNodeId = devices
      .filter { !it.config.containsKey(CONFIG_MY_SENSORS_NODE_ID) }
      .map { MissingNodeId(it) }

    return missingNodeId
  }

  class MissingNodeId(val device: DeviceConfig) : ValidationFailureReason() {
    override fun getDescription(): String {
      return "Device ${device.name} is MySensors-only device type. It requires '$CONFIG_MY_SENSORS_NODE_ID' in configuration which is " +
        "currently missing"
    }
  }
}
