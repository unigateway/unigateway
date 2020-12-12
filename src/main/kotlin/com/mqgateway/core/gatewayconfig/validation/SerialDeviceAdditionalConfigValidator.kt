package com.mqgateway.core.gatewayconfig.validation

import com.mqgateway.core.device.serial.PeriodicSerialInputDevice.Companion.CONFIG_ACCEPTABLE_PING_PERIOD_KEY
import com.mqgateway.core.device.serial.PeriodicSerialInputDevice.Companion.CONFIG_PERIOD_BETWEEN_ASK_KEY
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import javax.inject.Singleton

@Singleton
class SerialDeviceAdditionalConfigValidator : GatewayValidator {
  override fun validate(gateway: Gateway): List<ValidationFailureReason> {
    val devices: List<DeviceConfig> = gateway.rooms
      .flatMap { room -> room.points }
      .flatMap { point -> point.devices }
      .filter { device -> device.type in listOf(DeviceType.BME280) }

    val incorrectPeriodBetweenAskingForData = devices.filter { device ->
      val periodBetweenAsk = device.config[CONFIG_PERIOD_BETWEEN_ASK_KEY]?.toLong()
      periodBetweenAsk != null && (periodBetweenAsk < 10 || periodBetweenAsk > Int.MAX_VALUE)
    }.map { IncorrectPeriodBetweenAskingForData(it) }

    val incorrectAcceptablePingPeriod = devices.filter { device ->
      val acceptablePingPeriod = device.config[CONFIG_ACCEPTABLE_PING_PERIOD_KEY]?.toLong()
      acceptablePingPeriod != null && (acceptablePingPeriod < 10 || acceptablePingPeriod > Int.MAX_VALUE)
    }.map { IncorrectAcceptablePingPeriod(it) }

    return incorrectPeriodBetweenAskingForData + incorrectAcceptablePingPeriod
  }

  class IncorrectPeriodBetweenAskingForData(val device: DeviceConfig) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect '$CONFIG_PERIOD_BETWEEN_ASK_KEY' value for device: ${device.name}. Should be between 10 and ${Int.MAX_VALUE}."
    }
  }

  class IncorrectAcceptablePingPeriod(val device: DeviceConfig) : ValidationFailureReason() {

    override fun getDescription(): String {
      return "Incorrect '$CONFIG_ACCEPTABLE_PING_PERIOD_KEY' value for device: ${device.name}. Should be between 10 and ${Int.MAX_VALUE}."
    }
  }
}
