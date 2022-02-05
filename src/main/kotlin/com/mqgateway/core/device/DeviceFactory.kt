package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceConfig.UnexpectedDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.hardware.io.BinaryState
import com.mqgateway.core.hardware.provider.InputOutputProvider
import com.mqgateway.core.hardware.provider.Source
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.hwimpl.MqGatewayConnectorConfiguration
import com.pi4j.io.gpio.PinState
import java.time.Duration

class DeviceFactory(
  private val timersScheduler: TimersScheduler,
  private val systemInfoProvider: SystemInfoProvider,
  private val ioProvider: InputOutputProvider
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
          val closedState =
            deviceConfig.config[RelayDevice.CONFIG_CLOSED_STATE_KEY]?.let { BinaryState.valueOf(it) } ?: RelayDevice.CONFIG_CLOSED_STATE_DEFAULT
          val statusBinaryOutput = ioProvider.getBinaryOutput(Source.HARDWARE, MqGatewayConnectorConfiguration(portNumber, deviceConfig.wires[0]))
          RelayDevice(deviceConfig.id, statusBinaryOutput, closedState)
        }
        DeviceType.SWITCH_BUTTON -> {
          val debounceMs = deviceConfig.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: SwitchButtonDevice.CONFIG_DEBOUNCE_DEFAULT
          val statusBinaryInput =
            ioProvider.getBinaryInput(Source.HARDWARE, MqGatewayConnectorConfiguration(portNumber, deviceConfig.wires[0], debounceMs))
          val longPressTimeMs =
            deviceConfig.config[SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_KEY]?.toLong() ?: SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_DEFAULT
          SwitchButtonDevice(deviceConfig.id, statusBinaryInput, debounceMs, longPressTimeMs)
        }
        DeviceType.REED_SWITCH -> {
          val debounceMs = deviceConfig.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: ReedSwitchDevice.CONFIG_DEBOUNCE_DEFAULT
          val stateBinaryInput =
            ioProvider.getBinaryInput(Source.HARDWARE, MqGatewayConnectorConfiguration(portNumber, deviceConfig.wires[0], debounceMs))
          ReedSwitchDevice(deviceConfig.id, stateBinaryInput, debounceMs)
        }
        DeviceType.MOTION_DETECTOR -> {
          val debounceMs = deviceConfig.config[DigitalInputDevice.CONFIG_DEBOUNCE_KEY]?.toInt() ?: MotionSensorDevice.CONFIG_DEBOUNCE_DEFAULT
          val stateBinaryInput =
            ioProvider.getBinaryInput(Source.HARDWARE, MqGatewayConnectorConfiguration(portNumber, deviceConfig.wires[0], debounceMs))
          val motionSignalLevelString = deviceConfig.config[MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_KEY]
          val motionSignalLevel = motionSignalLevelString?.let { PinState.valueOf(it) } ?: MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT
          MotionSensorDevice(deviceConfig.id, stateBinaryInput, debounceMs, motionSignalLevel)
        }
        DeviceType.EMULATED_SWITCH -> {
          val stateBinaryOutput = ioProvider.getBinaryOutput(Source.HARDWARE, MqGatewayConnectorConfiguration(portNumber, deviceConfig.wires[0]))
          // TODO is it OK that device knows that source is HARDWARE? shouldn't it be unaware of the underlying hardware completely? Should we put it somewhere in the config?
          EmulatedSwitchButtonDevice(deviceConfig.id, stateBinaryOutput)
        }
        DeviceType.TIMER_SWITCH -> {
          val stateBinaryOutput = ioProvider.getBinaryOutput(Source.HARDWARE, MqGatewayConnectorConfiguration(portNumber, deviceConfig.wires[0]))
          TimerSwitchRelayDevice(deviceConfig.id, stateBinaryOutput, timersScheduler)
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
