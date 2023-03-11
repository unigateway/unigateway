package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration

class ShutterAdditionalConfigValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration): List<ValidationFailureReason> {
    val shutters: List<DeviceConfiguration> = gatewayConfiguration.devicesByType(DeviceType.SHUTTER)

    return shutters.flatMap { shutter ->
      val relaysValidationFailures = shutter.internalDevices.filterKeys { it in listOf("stopRelay", "upDownRelay") }
        .filter { (_, internalDevice) ->
          gatewayConfiguration.deviceById(internalDevice.referenceId)?.type != DeviceType.RELAY
        }.map { (name, internalDevice) ->
          NonRelayShutterInternalDevice(shutter, name, internalDevice.referenceId)
        }

      val buttonsValidationFailures = shutter.internalDevices.filterKeys { it in listOf("upButton", "downButton") }
        .filter { (_, internalDevice) ->
          gatewayConfiguration.deviceById(internalDevice.referenceId)?.type != DeviceType.SWITCH_BUTTON
        }.map { (name, internalDevice) ->
          NonButtonShutterInternalDevice(shutter, name, internalDevice.referenceId)
        }

      relaysValidationFailures + buttonsValidationFailures
    }
  }

  class NonRelayShutterInternalDevice(
    val device: DeviceConfiguration,
    val referenceName: String,
    val referencedDeviceId: String
  ) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Shutter '${device.name}' has incorrect internal device '$referenceName' referenced to '$referencedDeviceId' It should be RELAY."
    }
  }

  class NonButtonShutterInternalDevice(
    val device: DeviceConfiguration,
    val referenceName: String,
    val referencedDeviceId: String
  ) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Shutter '${device.name}' has incorrect internal device '$referenceName' referenced to '$referencedDeviceId' It should be SWITCH_BUTTON."
    }
  }
}
