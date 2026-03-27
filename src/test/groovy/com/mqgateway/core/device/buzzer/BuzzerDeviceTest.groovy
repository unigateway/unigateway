package com.mqgateway.core.device.buzzer

import com.mqgateway.core.hardware.simulated.SimulatedBinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class BuzzerDeviceTest extends Specification {
  def binaryOutput = new SimulatedBinaryOutput()

  @Subject
  BuzzerDevice buzzer = new BuzzerDevice("buzzer1", "Buzzer", binaryOutput, BinaryState.LOW, [:])

  def "should start and stop beeping"() {
    when:
    buzzer.change("state", "ON")

    then:
    binaryOutput.getState() == BinaryState.LOW

    when:
    buzzer.change("state", "OFF")

    then:
    binaryOutput.getState() == BinaryState.HIGH
  }

  def "should beep continuously for given number of seconds"() {
    when:
    buzzer.change("mode", "CONTINUOUS")
    buzzer.change("timer", "1")

    then:
    binaryOutput.getState() == BinaryState.LOW

    when:
    sleep(1200)

    then:
    binaryOutput.getState() == BinaryState.HIGH
  }

  def "should beep with breaks for given number of seconds"() {
    when:
    buzzer.change("mode", "INTERVAL")
    buzzer.change("timer", "1")

    then:
    binaryOutput.getState() in [BinaryState.LOW, BinaryState.HIGH]

    when:
    sleep(1200)

    then:
    binaryOutput.getState() == BinaryState.HIGH
  }

  def "should notify listeners on state and mode updates"() {
    given:
    def listenerStub = new UpdateListenerStub()
    buzzer.addListener(listenerStub)
    buzzer.init()

    when:
    buzzer.change("mode", "INTERVAL")
    buzzer.change("state", "ON")
    buzzer.change("state", "OFF")

    then:
    listenerStub.receivedUpdates.find { it.propertyId == "mode" } ==
      new UpdateListenerStub.Update("buzzer1", "mode", "INTERVAL")
    listenerStub.receivedUpdates.find { it.propertyId == "state" } ==
      new UpdateListenerStub.Update("buzzer1", "state", "ON")
    listenerStub.receivedUpdates.findAll { it.propertyId == "state" }.last() ==
      new UpdateListenerStub.Update("buzzer1", "state", "OFF")
  }
}
