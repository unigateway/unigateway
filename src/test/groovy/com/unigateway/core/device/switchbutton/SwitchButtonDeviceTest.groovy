package com.unigateway.core.device.switchbutton

import com.unigateway.core.hardware.simulated.SimulatedBinaryInput
import com.unigateway.core.io.BinaryState
import com.unigateway.utils.UpdateListenerStub
import com.unigateway.core.hardware.simulated.SimulatedBinaryInput
import com.unigateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class SwitchButtonDeviceTest extends Specification {

  public static final int LONG_PRESS_MS = 100
  def binaryInput = new SimulatedBinaryInput(BinaryState.HIGH)

  def conditions = new PollingConditions(initialDelay: 0.1, timeout: 1)

  @Subject
  SwitchButtonDevice device = new SwitchButtonDevice("button1", "Switch button", binaryInput, LONG_PRESS_MS, [:])

  def "should notify listeners on switch button pressed (LOW state)"() {
    given:
    def listenerStub = new UpdateListenerStub()
    device.addListener(listenerStub)
    device.init()

    when:
    binaryInput.setState(BinaryState.LOW)

    then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("button1", "state", "PRESSED")
  }

  def "should notify listeners on switch button released (HIGH state)"() {
    given:
    def listenerStub = new UpdateListenerStub()
    device.addListener(listenerStub)
    device.init()
    binaryInput.setState(BinaryState.LOW)

    when:
    binaryInput.setState(BinaryState.HIGH)

    then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("button1", "state", "RELEASED")
  }

  def "should notify listeners on switch button long pressed"() {
    given:
    def listenerStub = new UpdateListenerStub()
    device.addListener(listenerStub)
    device.init()

    when:
    binaryInput.low()

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.find { it.newValue == "LONG_PRESSED" }
    }
  }

  def "should notify listeners on switch button long released"() {
    given:
    def listenerStub = new UpdateListenerStub()
    device.addListener(listenerStub)
    device.init()
    binaryInput.low()
    conditions.eventually {
      assert listenerStub.receivedUpdates.find { it.newValue == "LONG_PRESSED" }
    }

    when:
    binaryInput.high()

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.find { it.newValue == "LONG_RELEASED" }
    }
  }
}
