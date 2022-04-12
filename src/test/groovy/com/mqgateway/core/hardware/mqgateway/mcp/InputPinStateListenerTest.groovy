package com.mqgateway.core.hardware.mqgateway.mcp

import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateChangeEvent
import java.time.Instant
import spock.lang.Specification

class InputPinStateListenerTest extends Specification {

  def "should notify listener on state change instantly when debounce is set to 0"() {
    given:
    boolean notified = false
    InputPinStateListener stateListener = new InputPinStateListener(0, { BinaryStateChangeEvent it -> notified = true })

    when:
    stateListener.handle(BinaryState.LOW, Instant.now())

    then:
    notified
  }

  def "should notify listener on state change when change took at least as long as debounce is set to"() {
    given:
    boolean notified = false
    InputPinStateListener stateListener = new InputPinStateListener(50, { BinaryStateChangeEvent it -> notified = true })
    def startTime = Instant.now()
    def endTime = startTime.plusMillis(50)

    when:
    stateListener.handle(BinaryState.LOW, startTime)
    stateListener.handle(BinaryState.LOW, endTime)
    stateListener.handle(BinaryState.HIGH, endTime.plusMillis(1))

    then:
    notified
  }

  def "should notify the listener on state change when change took longer then debounce and it was checked multiple times in between"() {
    given:
    boolean notified = false
    long debounceMs = 50
    InputPinStateListener stateListener = new InputPinStateListener(debounceMs, { BinaryStateChangeEvent it -> notified = true })
    def startTime = Instant.now()
    def endTime = startTime.plusMillis(debounceMs)

    when:
    stateListener.handle(BinaryState.LOW, startTime)
    stateListener.handle(BinaryState.LOW, startTime.plusMillis(20))
    stateListener.handle(BinaryState.LOW, startTime.plusMillis(30))
    stateListener.handle(BinaryState.LOW, startTime.plusMillis(40))
    stateListener.handle(BinaryState.LOW, endTime)
    stateListener.handle(BinaryState.HIGH, endTime.plusMillis(1))

    then:
    notified
  }

  def "should not notify listener on state change when change took less than debounce is set to"() {
    boolean notified = false
    InputPinStateListener stateListener = new InputPinStateListener(50, { BinaryStateChangeEvent it -> notified = true })
    def startTime = Instant.now()
    def endTime = startTime.plusMillis(49)

    when:
    stateListener.handle(BinaryState.LOW, startTime)
    stateListener.handle(BinaryState.HIGH, endTime)
    stateListener.handle(BinaryState.HIGH, endTime.plusMillis(1))

    then:
    !notified
  }

  def "should not notify listener about state change when state is flickering"() {
    boolean notified = false
    InputPinStateListener stateListener = new InputPinStateListener(50, { BinaryStateChangeEvent it -> notified = true })
    def startTime = Instant.now()

    when:
    stateListener.handle(BinaryState.LOW, startTime)
    stateListener.handle(BinaryState.HIGH, startTime.plusMillis(49))
    stateListener.handle(BinaryState.LOW, startTime.plusMillis(51))

    then:
    !notified
  }
}

