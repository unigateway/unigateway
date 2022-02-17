package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import java.time.Duration

class DeviceFactory(
  private val pinProvider: MqExpanderPinProvider,
  private val timersScheduler: TimersScheduler,
  private val systemInfoProvider: SystemInfoProvider
) {

  private val createdDevices: MutableMap<String, Device> = mutableMapOf()

  fun createAll(gatewayConfiguration: GatewayConfiguration): Set<Device> {
    val gatewayDevice = MqGatewayDevice(gatewayConfiguration.name, Duration.ofSeconds(30), systemInfoProvider)
    return setOf(gatewayDevice)
  }

  private fun create(
    portNumber: Int,
    deviceConfiguration: DeviceConfiguration,
    gatewayConfiguration: GatewayConfiguration
  ): Device {
    return createdDevices.getOrPut(deviceConfiguration.id) {
      when (deviceConfiguration.type) {
        DeviceType.REFERENCE -> {
          TODO()
        }
        DeviceType.RELAY -> {
          TODO()
        }
        DeviceType.SWITCH_BUTTON -> {
          TODO()
        }
        DeviceType.REED_SWITCH -> {
          TODO()
        }
        DeviceType.MOTION_DETECTOR -> {
          TODO()
        }
        DeviceType.EMULATED_SWITCH -> {
          TODO()
        }
        DeviceType.TIMER_SWITCH -> {
          TODO()
        }
        DeviceType.SHUTTER -> {
          TODO()
        }
        DeviceType.GATE -> {
          TODO()
        }
        DeviceType.MQGATEWAY -> throw IllegalArgumentException("MqGateway should never be specified as a separate device in configuration")
      }
    }
  }

  private fun createThreeButtonGateDevice(
    portNumber: Int,
    deviceConfiguration: DeviceConfiguration,
    gatewayConfiguration: GatewayConfiguration
  ): ThreeButtonsGateDevice {
    TODO()
  }

  private fun createSingleButtonGateDevice(
    portNumber: Int,
    deviceConfiguration: DeviceConfiguration,
    gatewayConfiguration: GatewayConfiguration
  ): SingleButtonsGateDevice {
    TODO()
  }
}
