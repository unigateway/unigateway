package com.mqgateway.core.device.serial

import com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.core.utils.TimersScheduler
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import mu.KotlinLogging
import java.time.Duration

private val LOGGER = KotlinLogging.logger {}

class DHT22PeriodicSerialInputDevice(
  id: String,
  toDevicePin: GpioPinDigitalOutput,
  fromDevicePin: GpioPinDigitalInput,
  serialConnection: SerialConnection,
  periodBetweenAskingForData: Duration = Duration.ofSeconds(CONFIG_PERIOD_BETWEEN_ASK_DEFAULT),
  acceptablePingPeriod: Duration = Duration.ofSeconds(CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT),
  scheduler: TimersScheduler
) : PeriodicSerialInputDevice(
  id,
  DeviceType.DHT22,
  toDevicePin,
  fromDevicePin,
  serialConnection,
  periodBetweenAskingForData,
  acceptablePingPeriod,
  scheduler
) {

  /**
   * Raw message format is "uptime_in_sec;temp_in_C;humidity;pressure"
   * e.g. 2073600;22.96;54.55
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
  }

  private fun parseMessage(message: String): DHT22Message? {
    return try {
      val parts = message.split(';')
      val uptime = parts[0].toInt()
      val temperatureInCelsius = parts[1].toFloat()
      val humidity = parts[2].toFloat()
      DHT22Message(uptime, temperatureInCelsius, humidity)
    } catch (e: Exception) {
      null
    }
  }

  data class DHT22Message(val uptimeInSec: Int, val temperatureInCelsius: Float, val humidity: Float)

  companion object {
    const val CONFIG_PERIOD_BETWEEN_ASK_KEY = "periodBetweenAskingForDataInSec"
    const val CONFIG_PERIOD_BETWEEN_ASK_DEFAULT = 60L * 3
    const val CONFIG_ACCEPTABLE_PING_PERIOD_KEY = "acceptablePingPeriodInSec"
    const val CONFIG_ACCEPTABLE_PING_PERIOD_DEFAULT = 60L
  }
}
