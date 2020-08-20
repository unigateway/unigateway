package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.serial.SerialConnection
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.fixedRateTimer

private val LOGGER = KotlinLogging.logger {}

@ObsoleteCoroutinesApi
abstract class PeriodicSerialInputDevice(
  id: String,
  type: DeviceType,
  private val toDevicePin: GpioPinDigitalOutput,
  private val fromDevicePin: GpioPinDigitalInput,
  private val serialConnection: SerialConnection,
  private val periodBetweenAskingForData: Duration = Duration.ofSeconds(60 * 3),
  private val acceptablePingPeriod: Duration = Duration.ofSeconds(60)
) : Device(id, type) {

  private var lastPingDate: LocalDateTime? = null

  override fun initDevice() {
    super.initDevice()
    toDevicePin.state = PinState.LOW

    fromDevicePin.addListener(GpioPinListenerDigital { event ->
      if (event.state == PinState.HIGH) { pingReceived() }
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
      messageReceived(message)
    } else {
      LOGGER.warn { "No ping from $id since ${acceptablePingPeriod.seconds} seconds. Not asking for data." }
    }
  }

  private fun pingReceived() {
    LOGGER.debug { "Ping from serial device $id received" }
    lastPingDate = LocalDateTime.now()
    notify("lastPing", lastPingDate.toString())
  }

  protected abstract fun messageReceived(message: String)
}

class SerialMessageNotReceivedException(deviceId: String) : RuntimeException("Requested data from device '$deviceId', but not received on time")
