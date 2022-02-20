package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import jakarta.inject.Singleton

@Singleton
class GateAdditionalConfigValidator : GatewayValidator {
  override fun validate(gateway: Gateway, systemProperties: GatewaySystemProperties): List<ValidationFailureReason> {
    val gates: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type in listOf(DeviceType.GATE) }

    return gates.flatMap { gate ->
      internalDeviceWithUnexpectedType(gate, BUTTON_NAMES, DeviceType.EMULATED_SWITCH, gateway) +
        internalDeviceWithUnexpectedType(gate, REED_SWITCHES_NAMES, DeviceType.REED_SWITCH, gateway)
    }
  }

  private fun internalDeviceWithUnexpectedType(
    gateDevice: DeviceConfig,
    internalDevicesNamesToCheck: List<String>,
    expectedType: DeviceType,
    gateway: Gateway
  ): List<UnexpectedGateInternalDevice> {

    return gateDevice.internalDevices
      .mapValues { deviceConfig ->
        deviceConfig.value.dereferenceIfNeeded(gateway)
      }.filter { internalDevice ->
        internalDevice.key in internalDevicesNamesToCheck && internalDevice.value.type != expectedType
      }.map { internalDevice ->
        UnexpectedGateInternalDevice(gateDevice, internalDevice.key, expectedType)
      }
  }

  class UnexpectedGateInternalDevice(
    val device: DeviceConfig,
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
