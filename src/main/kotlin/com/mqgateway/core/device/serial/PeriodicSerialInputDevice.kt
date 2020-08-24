package com.mqgateway.core.device.serial

import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.serial.SerialConnection
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.fixedRateTimer

private val LOGGER = KotlinLogging.logger {}

abstract class PeriodicSerialInputDevice(
  id: String,
  type: DeviceType,
  private val toDevicePin: GpioPinDigitalOutput,
  private val fromDevicePin: GpioPinDigitalInput,
  private val serialConnection: SerialConnection,
  private val periodBetweenAskingForData: Duration,
  private val acceptablePingPeriod: Duration
) : Device(id, type) {

  private var lastPingDate: LocalDateTime? = null

  override fun initDevice() {
    super.initDevice()
    toDevicePin.state = PinState.HIGH

    fromDevicePin.addListener(GpioPinListenerDigital { event ->
      if (event.state == PinState.LOW) { pingReceived() }
    })

    fixedRateTimer(name = "serial-$id-timer", daemon = true, period = periodBetweenAskingForData.toMillis()) {
      askForSerialDataIfDeviceIsAvailable()
    }
  }

  protected fun askForSerialDataIfDeviceIsAvailable() {
    if (lastPingDate?.isAfter(LocalDateTime.now().minus(acceptablePingPeriod)) == true) {
      LOGGER.debug { "Asking for data from device $id" }
      val message = runBlocking {
        serialConnection.askForData(id, toDevicePin)
      } ?: throw SerialMessageNotReceivedException(id)
      if (message.startsWith("error")) {
        val parts = message.split(';')
        if (parts[0] == "error") {
          LOGGER.error { "Device $id responded with an error: ${parts[1]}" }
        }
      } else {
        messageReceived(message)
      }
    } else {
      LOGGER.warn { "No ping from $id since ${acceptablePingPeriod.seconds} seconds. Not asking for data." }
    }
  }

  private fun pingReceived() {
    LOGGER.debug { "Ping from serial device $id received" }
    lastPingDate = LocalDateTime.now()
    notify("lastPing", lastPingDate.toString())
  }

  protected abstract fun messageReceived(rawMessage: String)
}

class SerialMessageNotReceivedException(deviceId: String) : RuntimeException("Requested data from device '$deviceId', but not received on time")
