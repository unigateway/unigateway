package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.hardware.MqGpioPinDigitalStateChangeEvent
import com.mqgateway.core.hardware.MqGpioPinListenerDigital
import com.mqgateway.core.hardware.MqPin
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import java.time.Clock
import java.time.Instant

class SimulatedPin : MqPin

class SimulatedGpioPinDigitalStateChangeEvent(val oldState: PinState?, private val newState: PinState) : MqGpioPinDigitalStateChangeEvent {

  override fun getState(): PinState {
    return newState
  }
}

abstract class SimulatedGpioPinDigital(initialState: PinState? = null, pullResistance: PinPullResistance = PinPullResistance.OFF) {

  protected var pinState: PinState? = initialState
  private val listeners: MutableList<MqGpioPinListenerDigital> = mutableListOf()
  private var pullResistance: PinPullResistance = PinPullResistance.OFF
  var clock: Clock = Clock.systemDefaultZone()
    set(value) {
      field = value
    }

  init {
    this.clock = Clock.systemDefaultZone()
    setPullResistance(pullResistance)
  }

  fun addListener(listener: MqGpioPinListenerDigital) {
    listeners.add(listener)
  }

  fun getState(): PinState = pinState ?: throw IllegalStateException("Should never ask for the state if it was not set before")

  open fun setState(newState: PinState) {
    if (this.pinState != newState && shouldNotify(this.pinState, newState)) {
      val oldState = this.pinState
      this.pinState = newState
      listeners.forEach {
        it.handleGpioPinDigitalStateChangeEvent(SimulatedGpioPinDigitalStateChangeEvent(oldState, newState))
      }
    }
  }

  protected open fun shouldNotify(from: PinState?, to: PinState): Boolean = true

  fun high() {
    setState(PinState.HIGH)
  }

  fun low() {
    setState(PinState.LOW)
  }

  fun setPullResistance(pull: PinPullResistance) {
    pullResistance = pull
    if (pinState == null) {
      when (pull) {
        PinPullResistance.PULL_UP -> setState(PinState.HIGH)
        PinPullResistance.PULL_DOWN -> setState(PinState.LOW)
        PinPullResistance.OFF -> {}
      }
    }
  }
}

class SimulatedGpioPinDigitalInput(resistance: PinPullResistance) : MqGpioPinDigitalInput, SimulatedGpioPinDigital(null, resistance) {

  private val debounce: MutableMap<PinState, Int> = mutableMapOf()
  private var lastStateChangeTime: Instant? = null

  override fun setDebounce(debounce: Int) {
    this.debounce[PinState.LOW] = debounce
    this.debounce[PinState.HIGH] = debounce
  }

  fun getDebounce(pinState: PinState): Int? = debounce[pinState]

  override fun setState(newState: PinState) {
    val previousState = this.pinState
    super.setState(newState)
    if (previousState != newState) {
      lastStateChangeTime = Instant.now(clock)
    }
  }

  override fun shouldNotify(from: PinState?, to: PinState): Boolean {
    if (lastStateChangeTime == null) {
      return true
    }
    return !Instant.now(clock).isBefore(lastStateChangeTime!!.plusMillis(debounce.getOrDefault(to, 0).toLong()))
  }
}

class SimulatedGpioPinDigitalOutput(initialState: PinState) : MqGpioPinDigitalOutput, SimulatedGpioPinDigital(initialState)
