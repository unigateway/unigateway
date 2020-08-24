package com.mqgateway.core.device

import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice
import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice.Companion.CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT
import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice.Companion.CONFIG_ACCEPTABLE_PING_PERIOD_KEY
import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice.Companion.CONFIG_PERIOD_BETWEEN_ASK_DEFAULT
import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice.Companion.CONFIG_PERIOD_BETWEEN_ASK_KEY
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.mcpexpander.ExpanderPinProvider
import com.mqgateway.core.serial.SerialConnection
import java.time.Duration

class DeviceFactory(private val pinProvider: ExpanderPinProvider, private val serialConnection: SerialConnection?) {

  fun createAll(gateway: Gateway): Set<Device> {
    return gateway.rooms
        .flatMap { it.points }
        .flatMap { point ->
          val portNumber = point.portNumber
          point.devices
            .filter { serialConnection != null || it.type != DeviceType.BME280 }
            .map { create(portNumber, it) }
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
      DeviceType.BME280 -> {

        serialConnection ?: throw SerialDisabledException(deviceConfig.id)

        val toDevicePin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires[0], deviceConfig.id + "_toDevicePin")
        val fromDevicePin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires[1], deviceConfig.id + "_fromDevicePin")
        val periodBetweenAskingForData =
          Duration.ofSeconds(deviceConfig.config?.get(CONFIG_PERIOD_BETWEEN_ASK_KEY)?.toLong() ?: CONFIG_PERIOD_BETWEEN_ASK_DEFAULT)
        val acceptablePingPeriod =
          Duration.ofSeconds(deviceConfig.config?.get(CONFIG_ACCEPTABLE_PING_PERIOD_KEY)?.toLong() ?: CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT)

        BME280PeriodicSerialInputDevice(
          deviceConfig.id,
          toDevicePin,
          fromDevicePin,
          serialConnection,
          periodBetweenAskingForData,
          acceptablePingPeriod
        )
      }
      DeviceType.SCT013 -> TODO()
      DeviceType.DHT22 -> TODO()
      DeviceType.EMULATED_SWITCH -> TODO()
    }
  }

  class SerialDisabledException(deviceId: String) :
    RuntimeException("Serial-related device '$deviceId' creation has been started, but serial is disabled in configuration")
}
