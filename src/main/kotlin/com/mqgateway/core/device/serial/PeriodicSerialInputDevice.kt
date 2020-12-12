package com.mqgateway.core.device.serial

import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DevicePropertyType.LAST_PING
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.core.utils.SerialDataListener
import com.mqgateway.core.utils.TimersScheduler
import com.pi4j.io.gpio.PinState
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

private val LOGGER = KotlinLogging.logger {}

abstract class PeriodicSerialInputDevice(
  id: String,
  type: DeviceType,
  private val toDevicePin: MqGpioPinDigitalOutput,
  private val fromDevicePin: MqGpioPinDigitalInput,
  private val serialConnection: SerialConnection,
  private val periodBetweenAskingForData: Duration,
  private val acceptablePingPeriod: Duration,
  private val scheduler: TimersScheduler
) : Device(id, type), TimersScheduler.SchedulableTimer, SerialDataListener {

  private var lastPingDate: LocalDateTime? = null
  private var lastAskForDataTime: LocalDateTime = LocalDateTime.now().minus(periodBetweenAskingForData)

  override fun initDevice() {
    super.initDevice()
    toDevicePin.setState(PinState.HIGH)

    fromDevicePin.addListener { event ->
      if (event.getState() == PinState.LOW) { pingReceived() }
    }

    scheduler.registerTimer(this)
  }

  override fun updateTimer(dateTime: LocalDateTime) {
    if (lastAskForDataTime.plus(periodBetweenAskingForData).isBefore(LocalDateTime.now())) {
      askForSerialDataIfDeviceIsAvailable()
      lastAskForDataTime = LocalDateTime.now()
    }
  }

  protected fun askForSerialDataIfDeviceIsAvailable() {
    if (lastPingDate?.isAfter(LocalDateTime.now().minus(acceptablePingPeriod)) == true) {
      LOGGER.debug { "Asking for data from device $id" }
      serialConnection.askForData(this@PeriodicSerialInputDevice)
    } else {
      LOGGER.warn { "No ping from $id since ${acceptablePingPeriod.seconds} seconds. Not asking for data." }
      notify(STATE, AVAILABILITY_OFFLINE_STATE)
    }
  }

  override fun askForDataPin() = toDevicePin
  override fun id() = id

  override fun onDataReceived(message: String?) {
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
    const val CONFIG_PERIOD_BETWEEN_ASK_KEY = "periodBetweenAskingForDataInSec"
    const val CONFIG_ACCEPTABLE_PING_PERIOD_KEY = "acceptablePingPeriodInSec"
  }
}
