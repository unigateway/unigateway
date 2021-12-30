package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceConfig.UnexpectedDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
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

  fun createAll(gateway: Gateway): Set<Device> {
    val gatewayDevice = MqGatewayDevice(gateway.name, Duration.ofSeconds(30), systemInfoProvider)
    return setOf(gatewayDevice) + gateway.rooms
      .flatMap { it.points }
      .flatMap { point ->
        val portNumber = point.portNumber
        point.devices.map { create(portNumber, it, gateway) }
      }.toSet()
  }

  private fun create(portNumber: Int, deviceConfig: DeviceConfig, gateway: Gateway): Device {
    return createdDevices.getOrPut(deviceConfig.id) {
      when (deviceConfig.type) {
        DeviceType.REFERENCE -> {
          val referencedDeviceConfig = deviceConfig.dereferenceIfNeeded(gateway)
          val referencedPortNumber = gateway.portNumberByDeviceId(referencedDeviceConfig.id)
          create(referencedPortNumber, referencedDeviceConfig, gateway)
        }
        DeviceType.RELAY -> {
          val triggerLevel =
            deviceConfig.config[RelayDevice.CONFIG_TRIGGER_LEVEL_KEY]?.let { PinState.valueOf(it) } ?: RelayDevice.CONFIG_TRIGGER_LEVEL_DEFAULT
          val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
          RelayDevice(deviceConfig.id, pin, triggerLevel)
        }
        DeviceType.SWITCH_BUTTON -> {
          val pin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
          val debounceMs = deviceConfig.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: SwitchButtonDevice.CONFIG_DEBOUNCE_DEFAULT
          val longPressTimeMs =
            deviceConfig.config[SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_KEY]?.toLong() ?: SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_DEFAULT
          SwitchButtonDevice(deviceConfig.id, pin, debounceMs, longPressTimeMs)
        }
        DeviceType.REED_SWITCH -> {
          val pin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
          val debounceMs = deviceConfig.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: ReedSwitchDevice.CONFIG_DEBOUNCE_DEFAULT
          ReedSwitchDevice(deviceConfig.id, pin, debounceMs)
        }
        DeviceType.MOTION_DETECTOR -> {
          val pin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
          val debounceMs = deviceConfig.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: MotionSensorDevice.CONFIG_DEBOUNCE_DEFAULT
          val motionSignalLevelString = deviceConfig.config[MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_KEY]
          val motionSignalLevel = motionSignalLevelString?.let { PinState.valueOf(it) } ?: MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT
          MotionSensorDevice(deviceConfig.id, pin, debounceMs, motionSignalLevel)
        }
        DeviceType.EMULATED_SWITCH -> {
          val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
          EmulatedSwitchButtonDevice(deviceConfig.id, pin)
        }
        DeviceType.TIMER_SWITCH -> {
          val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
          TimerSwitchRelayDevice(deviceConfig.id, pin, timersScheduler)
        }
        DeviceType.SHUTTER -> {
          val stopRelayDevice = create(portNumber, deviceConfig.internalDevices.getValue("stopRelay"), gateway) as RelayDevice
          val upDownRelayDevice = create(portNumber, deviceConfig.internalDevices.getValue("upDownRelay"), gateway) as RelayDevice
          ShutterDevice(
            deviceConfig.id,
            stopRelayDevice,
            upDownRelayDevice,
            deviceConfig.config.getValue("fullOpenTimeMs").toLong(),
            deviceConfig.config.getValue("fullCloseTimeMs").toLong()
          )
        }
        DeviceType.GATE -> {
          if (listOf("stopButton", "openButton", "closeButton").all { deviceConfig.internalDevices.containsKey(it) }) {
            createThreeButtonGateDevice(portNumber, deviceConfig, gateway)
          } else if (deviceConfig.internalDevices.containsKey("actionButton")) {
            createSingleButtonGateDevice(portNumber, deviceConfig, gateway)
          } else {
            throw UnexpectedDeviceConfigurationException(
              deviceConfig.id,
              "Gate device should have either three buttons defined (stopButton, openButton, closeButton) or single (actionButton)"
            )
          }
        }
        DeviceType.MQGATEWAY -> throw IllegalArgumentException("MqGateway should never be specified as a separate device in configuration")
      }
    }
  }

  private fun createThreeButtonGateDevice(portNumber: Int, deviceConfig: DeviceConfig, gateway: Gateway): ThreeButtonsGateDevice {
    val stopButton = create(portNumber, deviceConfig.internalDevices.getValue("stopButton"), gateway) as EmulatedSwitchButtonDevice
    val openButton = create(portNumber, deviceConfig.internalDevices.getValue("openButton"), gateway) as EmulatedSwitchButtonDevice
    val closeButton = create(portNumber, deviceConfig.internalDevices.getValue("closeButton"), gateway) as EmulatedSwitchButtonDevice
    val openReedSwitch = deviceConfig.internalDevices["openReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
    val closedReedSwitch = deviceConfig.internalDevices["closedReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
    return ThreeButtonsGateDevice(
      deviceConfig.id,
      stopButton,
      openButton,
      closeButton,
      openReedSwitch,
      closedReedSwitch
    )
  }

  private fun createSingleButtonGateDevice(portNumber: Int, deviceConfig: DeviceConfig, gateway: Gateway): SingleButtonsGateDevice {
    val actionButton = create(portNumber, deviceConfig.internalDevices.getValue("actionButton"), gateway) as EmulatedSwitchButtonDevice
    val openReedSwitch = deviceConfig.internalDevices["openReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
    val closedReedSwitch = deviceConfig.internalDevices["closedReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
    return SingleButtonsGateDevice(
      deviceConfig.id,
      actionButton,
      openReedSwitch,
      closedReedSwitch
    )
  }
}
