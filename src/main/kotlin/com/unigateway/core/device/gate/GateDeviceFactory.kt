package com.unigateway.core.device.gate

import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceFactory
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.UnexpectedDeviceConfigurationException
import com.unigateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.unigateway.core.device.reedswitch.ReedSwitchDevice

class GateDeviceFactory : DeviceFactory<GateDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.GATE
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): GateDevice {
    return if (listOf("stopButton", "openButton", "closeButton").all { deviceConfiguration.internalDevices.containsKey(it) }) {
      createThreeButtonGateDevice(deviceConfiguration, devices)
    } else if (deviceConfiguration.internalDevices.containsKey("actionButton")) {
      createSingleButtonGateDevice(deviceConfiguration, devices)
    } else {
      throw UnexpectedDeviceConfigurationException(
        deviceConfiguration.id,
        "Gate device should have either three buttons defined (stopButton, openButton, closeButton) or single (actionButton)"
      )
    }
  }

  private fun createThreeButtonGateDevice(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): ThreeButtonsGateDevice {
    val stopButton = getReferenceDevice(deviceConfiguration, "stopButton", devices) as EmulatedSwitchButtonDevice
    val openButton = getReferenceDevice(deviceConfiguration, "openButton", devices) as EmulatedSwitchButtonDevice
    val closeButton = getReferenceDevice(deviceConfiguration, "closeButton", devices) as EmulatedSwitchButtonDevice
    val openReedSwitch = deviceConfiguration.internalDevices["openReedSwitch"]
      ?.let { getReferenceDevice(deviceConfiguration, "openReedSwitch", devices) } as ReedSwitchDevice?
    val closedReedSwitch = deviceConfiguration.internalDevices["closedReedSwitch"]
      ?.let { getReferenceDevice(deviceConfiguration, "closedReedSwitch", devices) } as ReedSwitchDevice?

    return ThreeButtonsGateDevice(
      deviceConfiguration.id,
      deviceConfiguration.name,
      stopButton,
      openButton,
      closeButton,
      openReedSwitch,
      closedReedSwitch
    )
  }

  private fun createSingleButtonGateDevice(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): SingleButtonsGateDevice {
    val actionButton = getReferenceDevice(deviceConfiguration, "actionButton", devices) as EmulatedSwitchButtonDevice
    val openReedSwitch = deviceConfiguration.internalDevices["openReedSwitch"]
      ?.let { getReferenceDevice(deviceConfiguration, "openReedSwitch", devices) } as ReedSwitchDevice?
    val closedReedSwitch = deviceConfiguration.internalDevices["closedReedSwitch"]
      ?.let { getReferenceDevice(deviceConfiguration, "closedReedSwitch", devices) } as ReedSwitchDevice?
    return SingleButtonsGateDevice(
      deviceConfiguration.id,
      deviceConfiguration.name,
      actionButton,
      openReedSwitch,
      closedReedSwitch
    )
  }
}
