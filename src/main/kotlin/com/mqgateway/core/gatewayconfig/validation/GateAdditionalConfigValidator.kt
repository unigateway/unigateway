package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import jakarta.inject.Singleton

@Singleton
class GateAdditionalConfigValidator : GatewayValidator {
  override fun validate(gatewayConfiguration: GatewayConfiguration, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val gates: List<DeviceConfiguration> = gatewayConfiguration.devices
      .filter { device -> device.type in listOf(DeviceType.GATE) }

    return gates.flatMap { gate ->
      internalDeviceWithUnexpectedType(gate, BUTTON_NAMES, DeviceType.EMULATED_SWITCH, gatewayConfiguration) +
        internalDeviceWithUnexpectedType(gate, REED_SWITCHES_NAMES, DeviceType.REED_SWITCH, gatewayConfiguration)
    }
  }

  private fun internalDeviceWithUnexpectedType(
    gateDevice: DeviceConfiguration,
    internalDevicesNamesToCheck: List<String>,
    expectedType: DeviceType,
    gatewayConfiguration: GatewayConfiguration
  ): List<UnexpectedGateInternalDevice> {

    return gateDevice.internalDevices
      .filter { internalDeviceEntry ->
        val (key, internalDevice) = internalDeviceEntry
        key in internalDevicesNamesToCheck && gatewayConfiguration.devices.find { it.id == internalDevice.referenceId }!!.type != expectedType
      }.map { internalDevice ->
        UnexpectedGateInternalDevice(gateDevice, internalDevice.key, expectedType)
      }
  }

  class UnexpectedGateInternalDevice(
    val device: DeviceConfiguration,
    private val internalDeviceName: String,
    private val expectedInternalDeviceType: DeviceType
  ) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect internal device for gate: '${device.name}'. Internal device '$internalDeviceName' should be $expectedInternalDeviceType."
    }
  }

  companion object {
    val BUTTON_NAMES = listOf("actionButton", "stopButton", "openButton", "closeButton")
    val REED_SWITCHES_NAMES = listOf("closedReedSwitch", "openReedSwitch")
  }
}
