package com.mqgateway.core.device.timerswitch

import com.mqgateway.core.device.DataType
import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.DevicePropertyType.TIMER
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.DigitalOutputDevice
import com.mqgateway.core.device.timerswitch.TimerSwitchRelayDevice.TimerSwitchRelayState.CLOSED
import com.mqgateway.core.device.timerswitch.TimerSwitchRelayDevice.TimerSwitchRelayState.OPEN
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.utils.TimersScheduler
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val LOGGER = KotlinLogging.logger {}

class TimerSwitchRelayDevice(
  id: String,
  name: String,
  state: BinaryOutput,
  private val scheduler: TimersScheduler,
  config: Map<String, String> = emptyMap()
) :
  DigitalOutputDevice(
    id, name, DeviceType.TIMER_SWITCH, state,
    setOf(
      DeviceProperty(STATE, DataType.ENUM, "ON,OFF", retained = true),
      DeviceProperty(TIMER, DataType.INTEGER, "0:1440", settable = true, retained = true, unit = DataUnit.SECOND)
    ),
    config
  ),
  TimersScheduler.SchedulableTimer {

  private var turnOffTime: LocalDateTime = LocalDateTime.now()

  private fun changeRelayState(newState: TimerSwitchRelayState) {
    if (newState == CLOSED) {
      binaryOutput.setState(RELAY_CLOSED_STATE)
      notify(STATE, STATE_ON)
    } else {
      binaryOutput.setState(RELAY_OPEN_STATE)
      notify(STATE, STATE_OFF)
    }
  }

  override fun change(propertyId: String, newValue: String) {
    if (propertyId != TIMER.toString()) return

    val timerInMinutes = newValue.toLong()
    LOGGER.debug { "Changing property $propertyId on TimerSwitchRelayDevice $id to $newValue" }
    if (timerInMinutes > 0) {
      turnOffTime = LocalDateTime.now().plusMinutes(timerInMinutes)
      scheduler.registerTimer(this)
      changeRelayState(CLOSED)
    } else {
      changeRelayState(OPEN)
      scheduler.unregisterTimer(this)
    }
    notify(TIMER, newValue)
  }

  override fun updateTimer(dateTime: LocalDateTime) {
    if (turnOffTime.isBefore(dateTime)) {
      change(TIMER.toString(), 0.toString())
    } else {
      notify(TIMER, LocalDateTime.now().until(turnOffTime, ChronoUnit.MINUTES))
    }
  }

  enum class TimerSwitchRelayState {
    OPEN, CLOSED
  }

  companion object {
    val RELAY_CLOSED_STATE = BinaryState.LOW
    val RELAY_OPEN_STATE = RELAY_CLOSED_STATE.invert()
    const val STATE_ON = "ON"
    const val STATE_OFF = "OFF"
  }
}
