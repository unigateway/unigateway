package com.mqgateway.core.hardware.simulated

import com.mqgateway.core.hardware.MqGpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import java.time.Duration
import spock.lang.Specification
import spock.util.time.MutableClock

class SimulatedPinTest extends Specification {

  def "should notify all listeners when pin state changes"() {
    given:
    MqGpioPinDigitalStateChangeEvent event1 = null
    MqGpioPinDigitalStateChangeEvent event2 = null
    SimulatedGpioPinDigitalOutput pin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
    pin.addListener {event1 = it}
    pin.addListener {event2 = it}

    when:
    pin.low()

    then:
    event1.state == PinState.LOW
    event2.state == PinState.LOW
  }

  def "should not notify listener when pin state has not been changed even though method with change has been called"() {
    given:
    MqGpioPinDigitalStateChangeEvent event = null
    SimulatedGpioPinDigitalOutput pin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
    pin.addListener {event = it}

    when:
    pin.high()

    then:
    event == null
  }

  def "should initial state be #expectedState when pull resistance has been set to #pullResistance"(PinState expectedState, PinPullResistance pullResistance) {
    given:
    SimulatedGpioPinDigitalInput pin = new SimulatedGpioPinDigitalInput(pullResistance)

    when:
    def initialState = pin.getState()

    then:
    initialState == expectedState

    where:
    pullResistance              || expectedState
    PinPullResistance.PULL_DOWN || PinState.LOW
    PinPullResistance.PULL_UP   || PinState.HIGH
  }

  def "should throw exception when trying to get pin state without setting pull resistance nor state earlier"() {
    given:
    SimulatedGpioPinDigitalInput pin = new SimulatedGpioPinDigitalInput(PinPullResistance.OFF)

    when:
    pin.getState()

    then:
    thrown(IllegalStateException)
  }

  def "should not inform listener about state change if change was shorter than debounce"() {
    given:
    def stateUpdated = false
    SimulatedGpioPinDigitalInput pin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
    def clock = new MutableClock()
    pin.clock = clock
    int debounceMs = 1000
    pin.setDebounce(debounceMs)
    pin.addListener {stateUpdated = true}

    when:
    pin.setState(PinState.LOW)
    clock.plus(Duration.ofMillis(999))
    pin.setState(PinState.HIGH)

    then:
    !stateUpdated
  }
}
