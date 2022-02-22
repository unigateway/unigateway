package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import java.time.Duration

private const val STATE_CONNECTOR = "state" // TODO move to better place

class DeviceFactory(
  private val ioProvider: InputOutputProvider<*>,
  private val timersScheduler: TimersScheduler,
  private val systemInfoProvider: SystemInfoProvider
) {

  private val createdDevices: MutableMap<String, Device> = mutableMapOf()

  fun createAll(gatewayConfiguration: GatewayConfiguration): Set<Device> {
    val gatewayDevice = MqGatewayDevice(gatewayConfiguration.name, Duration.ofSeconds(30), systemInfoProvider)
    return setOf(gatewayDevice) + gatewayConfiguration.devices.map { create(it, gatewayConfiguration) }.toSet()
  }

  private fun create(deviceConfig: DeviceConfiguration, gatewayConfiguration: GatewayConfiguration): Device { // TODO probably no need to pass "gateway"
    return createdDevices.getOrPut(deviceConfig.id) {
      when (deviceConfig.type) {
        DeviceType.RELAY -> {
          val triggerLevel =
            deviceConfig.config[RelayDevice.CONFIG_CLOSED_STATE_KEY]?.let { BinaryState.valueOf(it) } ?: RelayDevice.CONFIG_CLOSED_STATE_DEFAULT
          val stateBinaryOutput = ioProvider.getBinaryOutput(deviceConfig.connectors[STATE_CONNECTOR]!!)
          RelayDevice(deviceConfig.id, stateBinaryOutput, triggerLevel)
        }
        DeviceType.SWITCH_BUTTON -> {
          val longPressTimeMs =
            deviceConfig.config[SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_KEY]?.toLong() ?: SwitchButtonDevice.CONFIG_LONG_PRESS_TIME_MS_DEFAULT
          val stateBinaryInput = ioProvider.getBinaryInput(deviceConfig.connectors[STATE_CONNECTOR]!!)
          SwitchButtonDevice(deviceConfig.id, stateBinaryInput, longPressTimeMs)
        }
        DeviceType.REED_SWITCH -> {
          val stateBinaryInput = ioProvider.getBinaryInput(deviceConfig.connectors[STATE_CONNECTOR]!!)
          ReedSwitchDevice(deviceConfig.id, stateBinaryInput)
        }
        DeviceType.MOTION_DETECTOR -> {
          val motionSignalLevelString = deviceConfig.config[MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_KEY]
          val motionSignalLevel = motionSignalLevelString?.let { BinaryState.valueOf(it) } ?: MotionSensorDevice.CONFIG_MOTION_SIGNAL_LEVEL_DEFAULT
          val stateBinaryInput = ioProvider.getBinaryInput(deviceConfig.connectors[STATE_CONNECTOR]!!)
          MotionSensorDevice(deviceConfig.id, stateBinaryInput, motionSignalLevel)
        }
        DeviceType.EMULATED_SWITCH -> {
          val stateBinaryOutput = ioProvider.getBinaryOutput(deviceConfig.connectors[STATE_CONNECTOR]!!)
          EmulatedSwitchButtonDevice(deviceConfig.id, stateBinaryOutput)
        }
        DeviceType.TIMER_SWITCH -> {
          val stateBinaryOutput = ioProvider.getBinaryOutput(deviceConfig.connectors[STATE_CONNECTOR]!!)
          TimerSwitchRelayDevice(deviceConfig.id, stateBinaryOutput, timersScheduler)
        }
        DeviceType.SHUTTER -> {
          // TODO how to build complex devices???
          TODO()
          // val stopRelayDevice = create(deviceConfig.internalDevices.getValue("stopRelay"), gateway) as RelayDevice
          // val upDownRelayDevice = create(deviceConfig.internalDevices.getValue("upDownRelay"), gateway) as RelayDevice
          // ShutterDevice(
          //   deviceConfig.id,
          //   stopRelayDevice,
          //   upDownRelayDevice,
          //   deviceConfig.config.getValue("fullOpenTimeMs").toLong(),
          //   deviceConfig.config.getValue("fullCloseTimeMs").toLong()
          // )
        }
        DeviceType.GATE -> {
          TODO()
          // if (listOf("stopButton", "openButton", "closeButton").all { deviceConfig.internalDevices.containsKey(it) }) {
          //   createThreeButtonGateDevice(portNumber, deviceConfig, gateway)
          // } else if (deviceConfig.internalDevices.containsKey("actionButton")) {
          //   createSingleButtonGateDevice(portNumber, deviceConfig, gateway)
          // } else {
          //   throw UnexpectedDeviceConfigurationException(
          //     deviceConfig.id,
          //     "Gate device should have either three buttons defined (stopButton, openButton, closeButton) or single (actionButton)"
          //   )
          // }
        }
        DeviceType.MQGATEWAY -> throw IllegalArgumentException("MqGateway should never be specified as a separate device in configuration")
      }
    }
  }

  // private fun createThreeButtonGateDevice(portNumber: Int, deviceConfig: DeviceConfig, gateway: Gateway): ThreeButtonsGateDevice {
  //   val stopButton = create(portNumber, deviceConfig.internalDevices.getValue("stopButton"), gateway) as EmulatedSwitchButtonDevice
  //   val openButton = create(portNumber, deviceConfig.internalDevices.getValue("openButton"), gateway) as EmulatedSwitchButtonDevice
  //   val closeButton = create(portNumber, deviceConfig.internalDevices.getValue("closeButton"), gateway) as EmulatedSwitchButtonDevice
  //   val openReedSwitch = deviceConfig.internalDevices["openReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
  //   val closedReedSwitch = deviceConfig.internalDevices["closedReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
  //   return ThreeButtonsGateDevice(
  //     deviceConfig.id,
  //     stopButton,
  //     openButton,
  //     closeButton,
  //     openReedSwitch,
  //     closedReedSwitch
  //   )
  // }
  //
  // private fun createSingleButtonGateDevice(portNumber: Int, deviceConfig: DeviceConfig, gateway: Gateway): SingleButtonsGateDevice {
  //   val actionButton = create(portNumber, deviceConfig.internalDevices.getValue("actionButton"), gateway) as EmulatedSwitchButtonDevice
  //   val openReedSwitch = deviceConfig.internalDevices["openReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
  //   val closedReedSwitch = deviceConfig.internalDevices["closedReedSwitch"]?.let { create(portNumber, it, gateway) } as ReedSwitchDevice?
  //   return SingleButtonsGateDevice(
  //     deviceConfig.id,
  //     actionButton,
  //     openReedSwitch,
  //     closedReedSwitch
  //   )
  // }
}
