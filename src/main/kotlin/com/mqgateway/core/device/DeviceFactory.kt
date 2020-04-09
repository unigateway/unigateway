package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.mcpexpander.ExpanderPinProvider

class DeviceFactory(private val pinProvider: ExpanderPinProvider) {

  fun createAll(gateway: Gateway): Set<Device> {
    return gateway.rooms
        .flatMap { it.points }
        .flatMap { point ->
          val portNumber = point.portNumber
          point.devices.map { create(portNumber, it) }
        }.toSet()
  }

  private fun create(portNumber: Int, deviceConfig: DeviceConfig): Device {

    return when (deviceConfig.type) {
      DeviceType.RELAY -> {
        val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
        RelayDevice(deviceConfig.id, pin)
      }
      DeviceType.SWITCH_BUTTON -> {
        val pin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
        val debounceMs = deviceConfig.config?.get(DigitalInputDevice.CONFIG_DEBOUNCE_KEY)?.toInt() ?: SwitchButtonDevice.CONFIG_DEBOUNCE_DEFAULT
        SwitchButtonDevice(deviceConfig.id, pin, debounceMs)
      }
      DeviceType.REED_SWITCH -> {
        val pin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
        val debounceMs = deviceConfig.config?.get(DigitalInputDevice.CONFIG_DEBOUNCE_KEY)?.toInt() ?: ReedSwitchDevice.CONFIG_DEBOUNCE_DEFAULT
        ReedSwitchDevice(deviceConfig.id, pin, debounceMs)
      }
      DeviceType.MOTION_DETECTOR -> {
        val pin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
        val debounceMs = deviceConfig.config?.get(DigitalInputDevice.CONFIG_DEBOUNCE_KEY)?.toInt() ?: MotionSensorDevice.CONFIG_DEBOUNCE_DEFAULT
        MotionSensorDevice(deviceConfig.id, pin, debounceMs)
      }
      DeviceType.BME280 -> TODO()
      DeviceType.SCT013 -> TODO()
      DeviceType.DHT22 -> TODO()
      DeviceType.EMULATED_SWITCH -> TODO()
    }
  }
}