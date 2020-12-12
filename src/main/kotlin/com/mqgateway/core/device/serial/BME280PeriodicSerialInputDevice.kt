package com.mqgateway.core.device.serial

import com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.core.utils.TimersScheduler
import mu.KotlinLogging
import java.time.Duration

private val LOGGER = KotlinLogging.logger {}

class BME280PeriodicSerialInputDevice(
  id: String,
  toDevicePin: MqGpioPinDigitalOutput,
  fromDevicePin: MqGpioPinDigitalInput,
  serialConnection: SerialConnection,
  periodBetweenAskingForData: Duration = Duration.ofSeconds(CONFIG_PERIOD_BETWEEN_ASK_DEFAULT),
  acceptablePingPeriod: Duration = Duration.ofSeconds(CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT),
  scheduler: TimersScheduler
) : PeriodicSerialInputDevice(
  id,
  DeviceType.BME280,
  toDevicePin,
  fromDevicePin,
  serialConnection,
  periodBetweenAskingForData,
  acceptablePingPeriod,
  scheduler
) {

  /**
   * Raw message format is "uptime_in_sec;temp_in_C;humidity;pressure"
   * e.g. 2073600;2296;54555;99241
   */
  override fun messageReceived(rawMessage: String) {
    val message = parseMessage(rawMessage)
    if (message == null) {
      LOGGER.warn("Message from device $id could not be parsed: $message")
      return
    }

    notify(UPTIME, message.uptimeInSec)
    notify(TEMPERATURE, message.temperatureInCelsius)
    notify(HUMIDITY, message.humidity)
    notify(PRESSURE, message.pressureInPa)
  }

  private fun parseMessage(message: String): BME280Message? {
    return try {
      val parts = message.split(';')
      val uptime = parts[0].toInt()
      val temperatureInCelsius = parts[1].toFloat() / 100
      val humidity = parts[2].toFloat() / 1000
      val pressureInPa = parts[3].toInt()
      BME280Message(uptime, temperatureInCelsius, humidity, pressureInPa)
    } catch (e: Exception) {
      null
    }
  }

  data class BME280Message(val uptimeInSec: Int, val temperatureInCelsius: Float, val humidity: Float, val pressureInPa: Int)

  companion object {
    const val CONFIG_PERIOD_BETWEEN_ASK_DEFAULT = 60L * 3
    const val CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT = 60L
  }
}
