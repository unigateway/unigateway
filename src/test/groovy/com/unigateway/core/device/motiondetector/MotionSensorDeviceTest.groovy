package com.unigateway.core.device.motiondetector

import com.unigateway.core.hardware.simulated.SimulatedBinaryInput
import com.unigateway.core.io.BinaryState
import com.unigateway.utils.UpdateListenerStub
import com.unigateway.core.hardware.simulated.SimulatedBinaryInput
import com.unigateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class MotionSensorDeviceTest extends Specification {

  SimulatedBinaryInput binaryInput = new SimulatedBinaryInput(BinaryState.LOW) // when sensor will be connected it will keep LOW state when no motion

  @Subject
  MotionSensorDevice device = new MotionSensorDevice("device1", "Motion sensor", binaryInput, BinaryState.HIGH, [:])

  def "should notify listeners on motion (HIGH state)"() {
    given:
    def listenerStub = new UpdateListenerStub()
    device.addListener(listenerStub)
    device.init()

    when:
    binaryInput.setState(BinaryState.HIGH)

    then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("device1", "state", "ON")
  }

  def "should notify listeners on motion stopped (LOW state)"() {
    given:
    def listenerStub = new UpdateListenerStub()
    device.addListener(listenerStub)
    device.init()
    binaryInput.setState(BinaryState.HIGH)

    when:
    binaryInput.setState(BinaryState.LOW)

    then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("device1", "state", "OFF")
  }

  def "should notify about motion when state is LOW when motionSignalLevel is set to LOW"() {
    given:
    MotionSensorDevice motionSensorWithLowOnMotion = new MotionSensorDevice("deviceLow", "Motion sensor", binaryInput, BinaryState.LOW, [:])
    def listenerStub = new UpdateListenerStub()
    motionSensorWithLowOnMotion.addListener(listenerStub)
    motionSensorWithLowOnMotion.init()

    when:
    binaryInput.setState(BinaryState.LOW)

    then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("deviceLow", "state", "ON")
  }
}
