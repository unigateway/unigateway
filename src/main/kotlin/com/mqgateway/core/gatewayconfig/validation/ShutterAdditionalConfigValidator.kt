package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class ShutterAdditionalConfigValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val shutters: List<DeviceConfiguration> = gatewayConfiguration.devices
      .filter { device -> device.type == DeviceType.SHUTTER }

    return shutters.filter { shutter ->
      shutter.internalDevices.values
        .any { internalDevice ->
          gatewayConfiguration.devices.find { it.id == internalDevice.referenceId }!!.type != DeviceType.RELAY
        }
    }.map { NonRelayShutterInternalDevice(it) }
  }

  class NonRelayShutterInternalDevice(val device: DeviceConfiguration) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect internal device for shutter: '${device.name}'. It should be RELAY."
    }
  }
}
