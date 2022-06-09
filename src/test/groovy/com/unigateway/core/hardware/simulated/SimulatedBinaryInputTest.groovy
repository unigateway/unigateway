package com.unigateway.core.hardware.simulated

import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.BinaryStateChangeEvent
import spock.lang.Specification

class SimulatedBinaryInputTest extends Specification {

  def "should notify all listeners when pin state changes"() {
    given:
    BinaryStateChangeEvent event1 = null
    BinaryStateChangeEvent event2 = null
    SimulatedBinaryInput binaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
    binaryInput.addListener { event1 = it }
    binaryInput.addListener { event2 = it }

    when:
    binaryInput.setState(BinaryState.LOW)

    then:
    event1.newState() == BinaryState.LOW
    event2.newState() == BinaryState.LOW
  }

  def "should not notify listener when pin state has not been changed even though method with change has been called"() {
    given:
    BinaryStateChangeEvent event = null
    SimulatedBinaryInput binaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
    binaryInput.addListener { event = it }

    when:
    binaryInput.setState(BinaryState.HIGH)

    then:
    event == null
  }
}
