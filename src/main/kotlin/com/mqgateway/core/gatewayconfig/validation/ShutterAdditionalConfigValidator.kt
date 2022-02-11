package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import javax.inject.Singleton

@Singleton
class ShutterAdditionalConfigValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val shutters: List<DeviceConfiguration> = gatewayConfiguration.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type == DeviceType.SHUTTER }

    return shutters.filter { shutter ->
      shutter.internalDevices.values
        .map { it.dereferenceIfNeeded(gatewayConfiguration) }
        .any { internalDevice ->
          internalDevice.type != DeviceType.RELAY
        }
    }.map { NonRelayShutterInternalDevice(it) }
  }

  class NonRelayShutterInternalDevice(val device: DeviceConfiguration) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect internal device for shutter: '${device.name}'. It should be RELAY."
    }
  }
}
