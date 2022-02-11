package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceConfiguration.UnexpectedDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.pi4j.io.gpio.PinState
import java.time.Duration

class DeviceFactory(
  private val pinProvider: MqExpanderPinProvider,
  private val timersScheduler: TimersScheduler,
  private val systemInfoProvider: SystemInfoProvider
) {

  private val createdDevices: MutableMap<String, Device> = mutableMapOf()

  fun createAll(gatewayConfiguration: GatewayConfiguration): Set<Device> {
    val gatewayDevice = MqGatewayDevice(gatewayConfiguration.name, Duration.ofSeconds(30), systemInfoProvider)
    return setOf(gatewayDevice) + gatewayConfiguration.rooms
      .flatMap { it.points }
      .flatMap { point ->
        val portNumber = point.portNumber
        point.devices.map { create(portNumber, it, gatewayConfiguration) }
      }.toSet()
  }

  private fun create(portNumber: Int, deviceConfiguration: DeviceConfiguration, gatewayConfiguration: GatewayConfiguration): Device {
    return createdDevices.getOrPut(deviceConfiguration.id) {
      when (deviceConfiguration.type) {
        DeviceType.REFERENCE -> {
          val referencedDeviceConfig = deviceConfiguration.dereferenceIfNeeded(gatewayConfiguration)
          val referencedPortNumber = gatewayConfiguration.portNumberByDeviceId(referencedDeviceConfig.id)
          create(referencedPortNumber, referencedDeviceConfig, gatewayConfiguration)
        }
        DeviceType.RELAY -> {
          val triggerLevel =
            deviceConfiguration.config[RelayDevice.CONFIG_TRIGGER_LEVEL_KEY]?.let { PinState.valueOf(it) } ?: RelayDevice.CONFIG_TRIGGER_LEVEL_DEFAULT
          val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfiguration.wires.first(), deviceConfiguration.id + "_pin")
          RelayDevice(deviceConfiguration.id, pin, triggerLevel)
        }
        DeviceType.SWITCH_BUTTON -> {
          val pin = pinProvider.pinDigitalInput(portNumber, deviceConfiguration.wires.first(), deviceConfiguration.id + "_pin")
          val debounceMs = deviceConfiguration.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: SwitchButtonDevice.CONFIG_DEBOUNCE_DEFAULT
          val longPressTimeMs =
            deviceConfiguration.config[SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_KEY]?.toLong() ?: SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_DEFAULT
          SwitchButtonDevice(deviceConfiguration.id, pin, debounceMs, longPressTimeMs)
        }
        DeviceType.REED_SWITCH -> {
          val pin = pinProvider.pinDigitalInput(portNumber, deviceConfiguration.wires.first(), deviceConfiguration.id + "_pin")
          val debounceMs = deviceConfiguration.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: ReedSwitchDevice.CONFIG_DEBOUNCE_DEFAULT
          ReedSwitchDevice(deviceConfiguration.id, pin, debounceMs)
        }
        DeviceType.MOTION_DETECTOR -> {
          val pin = pinProvider.pinDigitalInput(portNumber, deviceConfiguration.wires.first(), deviceConfiguration.id + "_pin")
          val debounceMs = deviceConfiguration.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: MotionSensorDevice.CONFIG_DEBOUNCE_DEFAULT
          val motionSignalLevelString = deviceConfiguration.config[MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_KEY]
          val motionSignalLevel = motionSignalLevelString?.let { PinState.valueOf(it) } ?: MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT
          MotionSensorDevice(deviceConfiguration.id, pin, debounceMs, motionSignalLevel)
        }
        DeviceType.EMULATED_SWITCH -> {
          val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfiguration.wires.first(), deviceConfiguration.id + "_pin")
          EmulatedSwitchButtonDevice(deviceConfiguration.id, pin)
        }
        DeviceType.TIMER_SWITCH -> {
          val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfiguration.wires.first(), deviceConfiguration.id + "_pin")
          TimerSwitchRelayDevice(deviceConfiguration.id, pin, timersScheduler)
        }
        DeviceType.SHUTTER -> {
          val stopRelayDevice = create(portNumber, deviceConfiguration.internalDevices.getValue("stopRelay"), gatewayConfiguration) as RelayDevice
          val upDownRelayDevice = create(portNumber, deviceConfiguration.internalDevices.getValue("upDownRelay"), gatewayConfiguration) as RelayDevice
          ShutterDevice(
            deviceConfiguration.id,
            stopRelayDevice,
            upDownRelayDevice,
            deviceConfiguration.config.getValue("fullOpenTimeMs").toLong(),
            deviceConfiguration.config.getValue("fullCloseTimeMs").toLong()
          )
        }
        DeviceType.GATE -> {
          if (listOf("stopButton", "openButton", "closeButton").all { deviceConfiguration.internalDevices.containsKey(it) }) {
            createThreeButtonGateDevice(portNumber, deviceConfiguration, gatewayConfiguration)
          } else if (deviceConfiguration.internalDevices.containsKey("actionButton")) {
            createSingleButtonGateDevice(portNumber, deviceConfiguration, gatewayConfiguration)
          } else {
            throw UnexpectedDeviceConfigurationException(
              deviceConfiguration.id,
              "Gate device should have either three buttons defined (stopButton, openButton, closeButton) or single (actionButton)"
            )
          }
        }
        DeviceType.MQGATEWAY -> throw IllegalArgumentException("MqGateway should never be specified as a separate device in configuration")
      }
    }
  }

  private fun createThreeButtonGateDevice(portNumber: Int, deviceConfiguration: DeviceConfiguration, gatewayConfiguration: GatewayConfiguration): ThreeButtonsGateDevice {
    val stopButton = create(portNumber, deviceConfiguration.internalDevices.getValue("stopButton"), gatewayConfiguration) as EmulatedSwitchButtonDevice
    val openButton = create(portNumber, deviceConfiguration.internalDevices.getValue("openButton"), gatewayConfiguration) as EmulatedSwitchButtonDevice
    val closeButton = create(portNumber, deviceConfiguration.internalDevices.getValue("closeButton"), gatewayConfiguration) as EmulatedSwitchButtonDevice
    val openReedSwitch = deviceConfiguration.internalDevices["openReedSwitch"]?.let { create(portNumber, it, gatewayConfiguration) } as ReedSwitchDevice?
    val closedReedSwitch = deviceConfiguration.internalDevices["closedReedSwitch"]?.let { create(portNumber, it, gatewayConfiguration) } as ReedSwitchDevice?
    return ThreeButtonsGateDevice(
      deviceConfiguration.id,
      stopButton,
      openButton,
      closeButton,
      openReedSwitch,
      closedReedSwitch
    )
  }

  private fun createSingleButtonGateDevice(portNumber: Int, deviceConfiguration: DeviceConfiguration, gatewayConfiguration: GatewayConfiguration): SingleButtonsGateDevice {
    val actionButton = create(portNumber, deviceConfiguration.internalDevices.getValue("actionButton"), gatewayConfiguration) as EmulatedSwitchButtonDevice
    val openReedSwitch = deviceConfiguration.internalDevices["openReedSwitch"]?.let { create(portNumber, it, gatewayConfiguration) } as ReedSwitchDevice?
    val closedReedSwitch = deviceConfiguration.internalDevices["closedReedSwitch"]?.let { create(portNumber, it, gatewayConfiguration) } as ReedSwitchDevice?
    return SingleButtonsGateDevice(
      deviceConfiguration.id,
      actionButton,
      openReedSwitch,
      closedReedSwitch
    )
  }
}
