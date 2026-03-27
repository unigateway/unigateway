package com.mqgateway.core.device.buzzer

import com.mqgateway.core.device.DataType.ENUM
import com.mqgateway.core.device.DataType.INTEGER
import com.mqgateway.core.device.DataUnit.SECOND
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType.MODE
import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.DevicePropertyType.TIMER
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.DigitalOutputDevice
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

private val LOGGER = KotlinLogging.logger {}

class BuzzerDevice(
  id: String,
  name: String,
  state: BinaryOutput,
  private val closedState: BinaryState,
  config: Map<String, String> = emptyMap(),
) :
  DigitalOutputDevice(
    id,
    name,
    DeviceType.BUZZER,
    state,
    setOf(
      DeviceProperty(STATE, ENUM, "ON,OFF", settable = true, retained = true),
      DeviceProperty(MODE, ENUM, "$MODE_CONTINUOUS,$MODE_INTERVAL", settable = true, retained = true),
      DeviceProperty(TIMER, INTEGER, "0:3600", settable = true, unit = SECOND),
    ),
    config,
  ) {
  private var timer = Timer("Buzzer_$id", false)
  private var intervalTimerTask: TimerTask? = null
  private var stopTimerTask: TimerTask? = null
  private var clock = Clock.systemDefaultZone()
  private var isToneOn = false
  private var currentMode: BuzzerMode = BuzzerMode.CONTINUOUS
  private var stateValue = STATE_OFF
  private var stopAtMillis: Long? = null

  override fun initProperty(
    propertyId: String,
    value: String,
  ) {
    when (propertyId) {
      MODE.toString(), STATE.toString() -> change(propertyId, value)
      else -> LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
    }
  }

  override fun change(
    propertyId: String,
    newValue: String,
  ) {
    LOGGER.debug { "Changing property $propertyId on buzzer $id to $newValue" }
    when (propertyId) {
      STATE.toString() -> {
        if (newValue.equals(STATE_ON, true)) {
          startBeep()
        } else {
          stopBeep()
        }
      }
      MODE.toString() -> {
        val newMode = BuzzerMode.fromValue(newValue)
        if (newMode == null) {
          LOGGER.warn { "Unsupported buzzer mode '$newValue' for device '$id'" }
          return
        }
        currentMode = newMode
        notify(MODE, currentMode.value)
        if (stateValue == STATE_ON) {
          val remainingMillis = stopAtMillis?.minus(clock.millis())?.coerceAtLeast(1)
          startBeep(remainingMillis)
        }
      }
      TIMER.toString() -> {
        val duration = newValue.toLongOrNull()
        if (duration == null || duration < 0) {
          LOGGER.warn { "Unsupported timer value '$newValue' for buzzer '$id'" }
          return
        }
        if (duration == 0L) {
          stopBeep()
        } else {
          startBeep(duration * 1000)
          notify(TIMER, duration)
        }
      }
      else -> LOGGER.warn { "Trying to change unsupported property '$id.$propertyId'" }
    }
  }

  private fun startBeep(durationMillis: Long? = null) {
    cancelFutures()

    when (currentMode) {
      BuzzerMode.CONTINUOUS -> setToneState(true)
      BuzzerMode.INTERVAL -> startIntervalBeep()
    }

    updateState(STATE_ON)
    stopAtMillis = durationMillis?.let { clock.millis() + it }
    durationMillis?.let {
      stopTimerTask =
        timer.schedule(it) {
          stopBeep()
          notify(TIMER, 0)
        }
    }
  }

  private fun stopBeep() {
    stopAtMillis = null
    cancelFutures()
    setToneState(false)
    updateState(STATE_OFF)
  }

  private fun startIntervalBeep() {
    setToneState(true)
    intervalTimerTask =
      timer.schedule(BEEP_INTERVAL_MILLIS, BEEP_INTERVAL_MILLIS) {
        setToneState(!isToneOn)
      }
  }

  private fun setToneState(on: Boolean) {
    isToneOn = on
    binaryOutput.setState(if (on) closedState else openState())
  }

  private fun openState(): BinaryState = closedState.invert()

  private fun cancelFutures() {
    intervalTimerTask?.cancel()
    intervalTimerTask = null

    stopTimerTask?.cancel()
    stopTimerTask = null
  }

  fun setTimerForTests(timer: Timer) {
    this.timer = timer
  }

  fun setClockForTests(clock: Clock) {
    this.clock = clock
  }

  private fun updateState(newState: String) {
    if (stateValue == newState) return
    stateValue = newState
    notify(STATE, newState)
  }

  enum class BuzzerMode(val value: String) {
    CONTINUOUS(MODE_CONTINUOUS),
    INTERVAL(MODE_INTERVAL),
    ;

    companion object {
      fun fromValue(value: String): BuzzerMode? {
        return entries.find { it.value.equals(value, true) }
      }
    }
  }

  companion object {
    const val CONFIG_CLOSED_STATE_KEY = "triggerLevel"
    val CONFIG_CLOSED_STATE_DEFAULT = BinaryState.HIGH

    const val STATE_ON = "ON"
    const val STATE_OFF = "OFF"

    const val MODE_CONTINUOUS = "CONTINUOUS"
    const val MODE_INTERVAL = "INTERVAL"

    const val BEEP_INTERVAL_MILLIS = 500L
  }
}
