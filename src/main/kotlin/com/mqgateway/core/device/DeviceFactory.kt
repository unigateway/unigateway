package com.mqgateway.core.device

import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice
import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice.Companion.CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT
import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice.Companion.CONFIG_PERIOD_BETWEEN_ASK_DEFAULT
import com.mqgateway.core.device.serial.DHT22PeriodicSerialInputDevice
import com.mqgateway.core.device.serial.PeriodicSerialInputDevice.Companion.CONFIG_ACCEPTABLE_PING_PERIOD_KEY
import com.mqgateway.core.device.serial.PeriodicSerialInputDevice.Companion.CONFIG_PERIOD_BETWEEN_ASK_KEY
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import java.time.Duration

class DeviceFactory(
  private val pinProvider: MqExpanderPinProvider,
  private val timersScheduler: TimersScheduler,
  private val serialConnection: SerialConnection?,
  private val systemInfoProvider: SystemInfoProvider
) {

  fun createAll(gateway: Gateway): Set<Device> {
    val gatewayDevice = MqGatewayDevice(gateway.name, Duration.ofSeconds(30), systemInfoProvider)
    return setOf(gatewayDevice) + gateway.rooms
        .flatMap { it.points }
        .flatMap { point ->
          val portNumber = point.portNumber
          point.devices
            .filter { serialConnection != null || !it.type.isSerialDevice() }
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
        MotionSensorDevice(deviceConfig.id, pin, debounceMs)
      }
      DeviceType.BME280 -> {

        serialConnection ?: throw SerialDisabledException(deviceConfig.id)

        val toDevicePin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires[0], deviceConfig.id + "_toDevicePin")
        val fromDevicePin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires[1], deviceConfig.id + "_fromDevicePin")
        val periodBetweenAskingForData =
          Duration.ofSeconds(deviceConfig.config[CONFIG_PERIOD_BETWEEN_ASK_KEY]?.toLong() ?: CONFIG_PERIOD_BETWEEN_ASK_DEFAULT)
        val acceptablePingPeriod =
          Duration.ofSeconds(deviceConfig.config[CONFIG_ACCEPTABLE_PING_PERIOD_KEY]?.toLong() ?: CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT)

        BME280PeriodicSerialInputDevice(
          deviceConfig.id,
          toDevicePin,
          fromDevicePin,
          serialConnection,
          periodBetweenAskingForData,
          acceptablePingPeriod,
          timersScheduler
        )
      }
      DeviceType.EMULATED_SWITCH -> {
        val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
        EmulatedSwitchButtonDevice(deviceConfig.id, pin)
      }
      DeviceType.DHT22 -> {
        serialConnection ?: throw SerialDisabledException(deviceConfig.id)

        val toDevicePin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires[0], deviceConfig.id + "_toDevicePin")
        val fromDevicePin = pinProvider.pinDigitalInput(portNumber, deviceConfig.wires[1], deviceConfig.id + "_fromDevicePin")
        val periodBetweenAskingForData =
          Duration.ofSeconds(deviceConfig.config[CONFIG_PERIOD_BETWEEN_ASK_KEY]?.toLong() ?: CONFIG_PERIOD_BETWEEN_ASK_DEFAULT)
        val acceptablePingPeriod =
          Duration.ofSeconds(deviceConfig.config[CONFIG_ACCEPTABLE_PING_PERIOD_KEY]?.toLong() ?: CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT)

        DHT22PeriodicSerialInputDevice(
          deviceConfig.id,
          toDevicePin,
          fromDevicePin,
          serialConnection,
          periodBetweenAskingForData,
          acceptablePingPeriod,
          timersScheduler
        )
      }
      DeviceType.TIMER_SWITCH -> {
        val pin = pinProvider.pinDigitalOutput(portNumber, deviceConfig.wires.first(), deviceConfig.id + "_pin")
        TimerSwitchRelayDevice(deviceConfig.id, pin, timersScheduler)
      }
      DeviceType.SHUTTER -> {
        val stopRelayDevice = create(portNumber, deviceConfig.internalDevices.getValue("stopRelay")) as RelayDevice
        val upDownRelayDevice = create(portNumber, deviceConfig.internalDevices.getValue("upDownRelay")) as RelayDevice
        ShutterDevice(
          deviceConfig.id, stopRelayDevice, upDownRelayDevice,
          deviceConfig.config.getValue("fullOpenTimeMs").toLong(), deviceConfig.config.getValue("fullCloseTimeMs").toLong()
        )
      }
      DeviceType.MQGATEWAY -> throw IllegalArgumentException("MqGateway should never be specified as a separate device in configuration")
      DeviceType.SCT013 -> TODO()
    }
  }

  class SerialDisabledException(deviceId: String) :
    RuntimeException("Serial-related device '$deviceId' creation has been started, but serial is disabled in configuration")
}
