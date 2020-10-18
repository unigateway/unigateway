package com.mqgateway.core.device

import com.mqgateway.core.device.RelayDevice.RelayState.CLOSED
import com.mqgateway.core.device.RelayDevice.RelayState.OPEN
import com.mqgateway.core.gatewayconfig.DevicePropertyType.POSITION
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import mu.KotlinLogging
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

private val LOGGER = KotlinLogging.logger {}

class ShutterDevice(
  id: String,
  private val stopRelay: RelayDevice,
  private val upDownRelay: RelayDevice,
  private val fullOpenTimeMs: Long,
  private val fullCloseTimeMs: Long
) : Device(id, DeviceType.SHUTTER) {

  private var currentPosition: Int? = null
    private set(value) {
      field = value
      value?.let { notify(POSITION, it) }
    }
  private var moveStartTime: Instant? = null
  private var state: State = State.UNKNOWN
    private set(value) {
      field = value
      notify(STATE, value.name)
    }

  private var stoppingTimer = Timer("ShutterMove_$id", false)
  private var scheduledStopTimerTask: TimerTask? = null
  private var clock = Clock.systemDefaultZone()

  override fun initProperty(propertyId: String, value: String) {
    if (propertyId != POSITION.toString()) {
      LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
      return
    }
    currentPosition = value.toInt()
    state = if (currentPosition == POSITION_CLOSED) {
      State.CLOSED
    } else {
      State.OPEN
    }
  }

  override fun initDevice() {
    super.initDevice()
    stopRelay.init(false)
    upDownRelay.init(false)
    if (currentPosition == null) {
      LOGGER.warn { "Shutter position is unknown. It will be initialized now by closing the shutter." }
      initializeCurrentPositionToClosed()
    }
  }

  private fun initializeCurrentPositionToClosed() {
    goDown()
    stoppingTimer.schedule(fullCloseTimeMs) {
      stop()
      currentPosition = 0
      state = State.CLOSED
    }
  }

  override fun change(propertyId: String, newValue: String) {
    if (currentPosition == null) {
      LOGGER.warn {
        "Current position in device $id is unknown. It is currently in state '$state'. " +
            "It is assumed that initialization is running, so this command will be ignored."
      }
      return
    }

    val actualCurrentPosition = calculateActualCurrentPosition()
    if (currentPosition != actualCurrentPosition) {
      LOGGER.debug { "New CurrentPosition found $actualCurrentPosition. Previous was $currentPosition." }
      currentPosition = actualCurrentPosition
    }

    val targetPosition: Int? = calculateTargetPosition(propertyId, newValue)
    if (targetPosition == null) {
      LOGGER.error { "Could not calculate target position for device $id (propertyId=$propertyId, newValue=$newValue)" }
      return
    }

    val requiredMove = calculateRequiredMove(currentPosition!!, targetPosition)

    scheduledStopTimerTask?.cancel()
    if (requiredMove.direction == Direction.UP) {
      LOGGER.info { "Command received to move shutter $id UP to position $targetPosition (${requiredMove}ms)" }
      goUp()
    } else if (requiredMove.direction == Direction.DOWN) {
      LOGGER.info { "Command received to move shutter $id DOWN to position $targetPosition (${requiredMove}ms)" }
      goDown()
    } else {
      return
    }

    scheduledStopTimerTask = stoppingTimer.schedule(requiredMove.time.toMillis()) {
      LOGGER.info { "Stopping shutter $id after move" }
      stop()
      currentPosition = targetPosition
      state = if (currentPosition == POSITION_CLOSED) {
        State.CLOSED
      } else {
        State.OPEN
      }
    }
  }

  private fun calculateRequiredMove(currentPosition: Int, targetPosition: Int): Move {
    val positionDifferenceToMove = targetPosition - currentPosition
    val requiredDirection = when {
      positionDifferenceToMove > 0 -> Direction.UP
      positionDifferenceToMove < 0 -> Direction.DOWN
      else -> Direction.STAY
    }
    val goUpOrDownTimeMs = when (requiredDirection) {
      Direction.UP -> fullOpenTimeMs
      Direction.DOWN -> fullCloseTimeMs
      Direction.STAY -> 0
    }

    val timeMs = if (requiredDirection != Direction.STAY && (targetPosition == POSITION_OPEN || targetPosition == POSITION_CLOSED)) {
      goUpOrDownTimeMs
    } else {
      (goUpOrDownTimeMs * (positionDifferenceToMove.absoluteValue.toFloat() / 100)).toLong()
    }

    return Move(requiredDirection, Duration.ofMillis(timeMs))
  }

  private fun calculateTargetPosition(propertyId: String, newValue: String): Int? {
    return when (propertyId) {
      STATE.toString() -> {
        when (newValue) {
          Command.OPEN.name -> POSITION_OPEN
          Command.CLOSE.name -> POSITION_CLOSED
          Command.STOP.name -> currentPosition
          else -> null
        }
      }
      POSITION.toString() -> {
        newValue.toIntOrNull()
      }
      else -> {
        LOGGER.warn { "Trying to change unsupported property '$id.$propertyId'" }
        null
      }
    }
  }

  private fun calculateActualCurrentPosition(): Int {
    return when (state) {
      State.OPEN -> currentPosition!!
      State.CLOSED -> currentPosition!!
      State.OPENING -> {
        val moveTimeMs = Duration.between(moveStartTime, Instant.now(clock)).toMillis()
        val percentageMoved: Int = ((moveTimeMs.toFloat() / fullOpenTimeMs) * 100).toInt()
        min(currentPosition!! + percentageMoved, 100)
      }
      State.CLOSING -> {
        val moveTimeMs = Duration.between(moveStartTime, Instant.now(clock)).toMillis()
        val percentageMoved: Int = ((moveTimeMs.toFloat() / fullCloseTimeMs) * 100).toInt()
        max(currentPosition!! - percentageMoved, 0)
      }
      State.UNKNOWN -> throw IllegalStateException("Shutter $id is in state: $state which is unexpected. Unable to continue.")
    }
  }

  private fun goDown() {
    upDownRelay.changeState(OPEN)
    stopRelay.changeState(CLOSED)
    moveStartTime = Instant.now(clock)
    state = State.CLOSING
  }

  private fun goUp() {
    upDownRelay.changeState(CLOSED)
    stopRelay.changeState(CLOSED)
    moveStartTime = Instant.now(clock)
    state = State.OPENING
  }

  private fun stop() {
    stopRelay.changeState(OPEN)
    upDownRelay.changeState(OPEN)
    moveStartTime = null
  }

  fun setStoppingTimerForTests(timer: Timer) {
    this.stoppingTimer = timer
  }

  fun setClockForTests(clock: Clock) {
    this.clock = clock
  }

  companion object {
    const val POSITION_CLOSED = 0
    const val POSITION_OPEN = 100
  }

  enum class Command {
    OPEN, CLOSE, STOP
  }

  enum class State {
    OPENING, CLOSING, OPEN, CLOSED, UNKNOWN
  }

  data class Move(val direction: Direction, val time: Duration)

  enum class Direction {
    UP, DOWN, STAY
  }
}
