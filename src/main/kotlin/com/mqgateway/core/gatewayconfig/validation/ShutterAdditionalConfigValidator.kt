package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import javax.inject.Singleton

@Singleton
class ShutterAdditionalConfigValidator : GatewayValidator {
  override fun validate(gateway: Gateway): List<ValidationFailureReason> {
    val shutters: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type in listOf(DeviceType.SHUTTER) }

    return shutters.filter { shutter ->
      shutter.internalDevices.values.any { internalDevice ->
        internalDevice.type != DeviceType.RELAY
      }
    }.map { NonRelayShutterInternalDevice(it) }
  }

  class NonRelayShutterInternalDevice(val device: DeviceConfig) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect internal device for shutter: '${device.name}'. It should be RELAY."
    }
  }
}
