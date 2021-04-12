package com.mqgateway.core.device

import static com.mqgateway.core.device.EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE
import static com.mqgateway.core.device.EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE
import static com.mqgateway.core.device.ThreeButtonsGateDevice.CLOSED_STATE_VALUE
import static com.mqgateway.core.device.ThreeButtonsGateDevice.CLOSING_STATE_VALUE
import static com.mqgateway.core.device.ThreeButtonsGateDevice.OPENING_STATE_VALUE
import static com.mqgateway.core.device.ThreeButtonsGateDevice.OPEN_STATE_VALUE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class ThreeButtonsGateDeviceTest extends Specification {
  def stopPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
  EmulatedSwitchButtonDevice stopButton = new EmulatedSwitchButtonDevice("stopButton", stopPin, 10)
  def openPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
  EmulatedSwitchButtonDevice openButton = new EmulatedSwitchButtonDevice("openButton", openPin, 500)
  def closePin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
  EmulatedSwitchButtonDevice closeButton = new EmulatedSwitchButtonDevice("closeButton", closePin, 500)

  def openReedSwitchPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
  ReedSwitchDevice openReedSwitch = new ReedSwitchDevice("openReedSwitch", openReedSwitchPin, 0)
  def closedReedSwitchPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
  ReedSwitchDevice closedReedSwitch = new ReedSwitchDevice("closedReedSwitch", closedReedSwitchPin, 0)

  @Subject
  ThreeButtonsGateDevice gateDevice = new ThreeButtonsGateDevice("testGate", stopButton, openButton, closeButton, openReedSwitch, closedReedSwitch)

  UpdateListenerStub listenerStub = new UpdateListenerStub()

  def conditions = new PollingConditions(delay: 0.01)

  def "should initialize state with property initialized state when none of the reed switches report state"() {
    given:
    gateDevice.initProperty(STATE.toString(), "OPEN")
    gateDevice.addListener(listenerStub)
    closedReedSwitchPin.state = PinState.HIGH
    openReedSwitchPin.state = PinState.HIGH

    when:
    gateDevice.init()

    then:
    listenerStub.receivedUpdates == [] // no state changes means it has been left as initProperty
  }

  def "should activate stopButton and then openButton emulated switches on state changed to OPEN"() {
    given:
    openButton.addListener(listenerStub)
    closeButton.addListener(listenerStub)
    stopButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    closedReedSwitchPin.state = PinState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "OPEN")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.subList(0, 3) == [
        new UpdateListenerStub.Update("stopButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("stopButton", STATE.toString(), RELEASED_STATE_VALUE),
        new UpdateListenerStub.Update("openButton", STATE.toString(), PRESSED_STATE_VALUE)
      ]
    }
  }

  def "should activate stopButton and then closeButton emulated switches on state changed to CLOSE"() {
    given:
    openButton.addListener(listenerStub)
    closeButton.addListener(listenerStub)
    stopButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    openReedSwitchPin.state = PinState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "CLOSE")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.subList(0, 3) == [
        new UpdateListenerStub.Update("stopButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("stopButton", STATE.toString(), RELEASED_STATE_VALUE),
        new UpdateListenerStub.Update("closeButton", STATE.toString(), PRESSED_STATE_VALUE)
      ]
    }
  }

  def "should activate stopButton emulated switch on state changed to STOP"() {
    given:
    openButton.addListener(listenerStub)
    closeButton.addListener(listenerStub)
    stopButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    openReedSwitchPin.state = PinState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "STOP")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("stopButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("stopButton", STATE.toString(), RELEASED_STATE_VALUE)
      ]
    }
  }

  def "should initial gate state be set to CLOSED and no change done when closed reed switch reports closed gate"() {
    given:
    gateDevice.addListener(listenerStub)
    openButton.addListener(listenerStub)
    closeButton.addListener(listenerStub)
    stopButton.addListener(listenerStub)
    closedReedSwitchPin.state = PinState.LOW // Gate closed

    when:
    gateDevice.init()

    then:
    listenerStub.receivedUpdates == [ new UpdateListenerStub.Update("testGate", STATE.toString(), "CLOSED") ]
  }

  def "should close the gate at init if initial position is unknown because none of the reed switches values is set"() {
    given:
    gateDevice.addListener(listenerStub)

    when:
    gateDevice.init()

    then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("testGate", STATE.toString(), "CLOSING")
  }

  def "should set initial state to opened if open reed switch report gate is open"() {
    given:
    gateDevice.addListener(listenerStub)
    openButton.addListener(listenerStub)
    closeButton.addListener(listenerStub)
    stopButton.addListener(listenerStub)
    openReedSwitchPin.state = PinState.LOW // Gate open

    when:
    gateDevice.init()

    then:
    listenerStub.receivedUpdates == [ new UpdateListenerStub.Update("testGate", STATE.toString(), "OPEN") ]
  }

  def "should initialize 'state' property"() {
    given:
    gateDevice.addListener(listenerStub)

    when:
    gateDevice.initProperty(STATE.toString(), "OPEN")

    then:
    listenerStub.receivedUpdates == [new UpdateListenerStub.Update("testGate", STATE.toString(), "OPEN")]
  }

  def "should not fail nor do any action when unknown property has changed"() {
    given:
    openButton.addListener(listenerStub)
    closeButton.addListener(listenerStub)
    stopButton.addListener(listenerStub)
    gateDevice.addListener(listenerStub)
    closedReedSwitchPin.state = PinState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change("nonexisting", "OPEN")

    then:
    listenerStub.receivedUpdates == [new UpdateListenerStub.Update("testGate", STATE.toString(), "CLOSED")]
    notThrown()
  }

  def "should set state to CLOSING when sent state change to CLOSE and closedReedSwitch is configured"() {
    given:
    gateDevice.addListener(listenerStub)
    openReedSwitchPin.state = PinState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "CLOSE")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPEN_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSING_STATE_VALUE)
      ]
    }
  }

  def "should set state to CLOSED immediately when sent state change to CLOSE and closedReedSwitch is not configured"() {
    given:
    def gateDevice = new ThreeButtonsGateDevice("testGate", stopButton, openButton, closeButton, openReedSwitch, null)
    gateDevice.addListener(listenerStub)
    openReedSwitchPin.state = PinState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "CLOSE")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPEN_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSED_STATE_VALUE)
      ]
    }
  }

  def "should set state to CLOSED after reed switch reports that gate is closed when closedReedSwitch is configured"() {
    given:
    gateDevice.addListener(listenerStub)
    openReedSwitchPin.state = PinState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "CLOSE")
    closedReedSwitchPin.state = PinState.LOW // emulates reed switch closed state

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPEN_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSING_STATE_VALUE),
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSED_STATE_VALUE)
      ]
    }
  }

  def "should set state to OPENING when sent state change to OPEN and openReedSwitch is configured"() {
    given:
    gateDevice.addListener(listenerStub)
    closedReedSwitchPin.state = PinState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "OPEN")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSED_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPENING_STATE_VALUE)
      ]
    }
  }

  def "should set state to OPEN immediately when sent state change to OPEN and openReedSwitch is not configured"() {
    given:
    def gateDevice = new ThreeButtonsGateDevice("testGate", stopButton, openButton, closeButton, null, closedReedSwitch)
    gateDevice.addListener(listenerStub)
    closedReedSwitchPin.state = PinState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "OPEN")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSED_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPEN_STATE_VALUE)
      ]
    }
  }

  def "should set state to OPEN after reed switch reports that gate is closed when openReedSwitch is configured"() {
    given:
    gateDevice.addListener(listenerStub)
    closedReedSwitchPin.state = PinState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "OPEN")
    openReedSwitchPin.state = PinState.LOW // emulates reed switch open state

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSED_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPENING_STATE_VALUE),
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPEN_STATE_VALUE)
      ]
    }
  }
}
