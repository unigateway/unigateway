package com.mqgateway.core.device.serial

import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DevicePropertyType.LAST_PING
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.utils.SerialConnection
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
      }
      if (message == null) {
        LOGGER.error { "Could not get message from serial device $id" }
      } else if (message.startsWith("error")) {
        val parts = message.split(';')
        if (parts[0] == "error") {
          LOGGER.error { "Device $id responded with an error: ${parts[1]}" }
        }
      } else {
        notify(STATE, AVAILABILITY_ONLINE_STATE)
        messageReceived(message)
      }
    } else {
      LOGGER.warn { "No ping from $id since ${acceptablePingPeriod.seconds} seconds. Not asking for data." }
      notify(STATE, AVAILABILITY_OFFLINE_STATE)
    }
  }

  private fun pingReceived() {
    LOGGER.debug { "Ping from serial device $id received" }
    lastPingDate = LocalDateTime.now()
    notify(LAST_PING, lastPingDate.toString())
  }

  protected abstract fun messageReceived(rawMessage: String)

  companion object {
    const val AVAILABILITY_ONLINE_STATE = "ONLINE"
    const val AVAILABILITY_OFFLINE_STATE = "OFFLINE"
  }
}
