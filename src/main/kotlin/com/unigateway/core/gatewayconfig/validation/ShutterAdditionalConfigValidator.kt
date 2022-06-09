package com.unigateway.core.gatewayconfig.validation

import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.gatewayconfig.GatewayConfiguration

class ShutterAdditionalConfigValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    val shutters: List<DeviceConfiguration> = gatewayConfiguration.devicesByType(DeviceType.SHUTTER)

    return shutters.filter { shutter ->
      shutter.internalDevices.values
        .any { internalDevice ->
          gatewayConfiguration.deviceById(internalDevice.referenceId)?.type != DeviceType.RELAY
        }
    }.map { NonRelayShutterInternalDevice(it) }
  }

  class NonRelayShutterInternalDevice(val device: DeviceConfiguration) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect internal device for shutter: '${device.name}'. It should be RELAY."
    }
  }
}
