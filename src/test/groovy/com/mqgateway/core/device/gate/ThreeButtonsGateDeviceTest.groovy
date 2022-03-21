package com.mqgateway.core.device.gate

import static com.mqgateway.core.device.DevicePropertyType.STATE
import static com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE
import static com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE
import static com.mqgateway.core.device.gate.ThreeButtonsGateDevice.CLOSED_STATE_VALUE
import static com.mqgateway.core.device.gate.ThreeButtonsGateDevice.CLOSING_STATE_VALUE
import static com.mqgateway.core.device.gate.ThreeButtonsGateDevice.OPENING_STATE_VALUE
import static com.mqgateway.core.device.gate.ThreeButtonsGateDevice.OPEN_STATE_VALUE

import com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.mqgateway.core.device.reedswitch.ReedSwitchDevice
import com.mqgateway.core.hardware.simulated.SimulatedBinaryInput
import com.mqgateway.core.hardware.simulated.SimulatedBinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class ThreeButtonsGateDeviceTest extends Specification {
  def stopBinaryOutput = new SimulatedBinaryOutput()
  EmulatedSwitchButtonDevice stopButton = new EmulatedSwitchButtonDevice("stopButton", "Stop button", stopBinaryOutput, 10, [:])
  def openBinaryOutput = new SimulatedBinaryOutput()
  EmulatedSwitchButtonDevice openButton = new EmulatedSwitchButtonDevice("openButton", "Open button", openBinaryOutput, 500, [:])
  def closeBinaryOutput = new SimulatedBinaryOutput()
  EmulatedSwitchButtonDevice closeButton = new EmulatedSwitchButtonDevice("closeButton", "Close button", closeBinaryOutput, 500, [:])

  def openReedSwitchBinaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
  ReedSwitchDevice openReedSwitch = new ReedSwitchDevice("openReedSwitch", "Open reed switch", openReedSwitchBinaryInput, [:])
  def closedReedSwitchBinaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
  ReedSwitchDevice closedReedSwitch = new ReedSwitchDevice("closedReedSwitch", "Closed reed switch", closedReedSwitchBinaryInput, [:])

  @Subject
  ThreeButtonsGateDevice gateDevice = new ThreeButtonsGateDevice("testGate", "Three buttons gate", stopButton, openButton, closeButton, openReedSwitch, closedReedSwitch, [:])

  UpdateListenerStub listenerStub = new UpdateListenerStub()

  def conditions = new PollingConditions(delay: 0.01)

  def "should initialize state with property initialized state when none of the reed switches report state"() {
    given:
    gateDevice.initProperty(STATE.toString(), "OPEN")
    gateDevice.addListener(listenerStub)
    closedReedSwitchBinaryInput.state = BinaryState.HIGH
    openReedSwitchBinaryInput.state = BinaryState.HIGH

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
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
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
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
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
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
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
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed

    when:
    gateDevice.init()

    then:
    listenerStub.receivedUpdates == [new UpdateListenerStub.Update("testGate", STATE.toString(), "CLOSED")]
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
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open

    when:
    gateDevice.init()

    then:
    listenerStub.receivedUpdates == [new UpdateListenerStub.Update("testGate", STATE.toString(), "OPEN")]
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
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
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
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
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
    def gateDevice = new ThreeButtonsGateDevice("testGate", "Three buttons gate", stopButton, openButton, closeButton, openReedSwitch, null, [:])
    gateDevice.addListener(listenerStub)
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
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
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "CLOSE")
    closedReedSwitchBinaryInput.state = BinaryState.LOW // emulates reed switch closed state

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
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
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
    def gateDevice = new ThreeButtonsGateDevice("testGate", "Three buttons gate", stopButton, openButton, closeButton, null, closedReedSwitch, [:])
    gateDevice.addListener(listenerStub)
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
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
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "OPEN")
    openReedSwitchBinaryInput.state = BinaryState.LOW // emulates reed switch open state

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("testGate", STATE.toString(), CLOSED_STATE_VALUE), // initialization state change
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPENING_STATE_VALUE),
        new UpdateListenerStub.Update("testGate", STATE.toString(), OPEN_STATE_VALUE)
      ]
    }
  }

  @Unroll
  def "should change gate state when reed switch reports #whenDescription"() {
    given:
    ThreeButtonsGateDevice gateDevice = new ThreeButtonsGateDevice("testGate", "Three buttons gate",stopButton, openButton, closeButton,
                                                                   hasOpenReedSwitch ? openReedSwitch : null,
                                                                   hasClosedReedSwitch ? closedReedSwitch : null, [:])
    gateDevice.addListener(listenerStub)
    if (initialState == 'OPEN') {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH
      openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
      if (hasOpenReedSwitch) {
        expectedUpdates.add(0, "OPEN")
      }
    } else if (initialState == 'CLOSED') {
      closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
      openReedSwitchBinaryInput.state = BinaryState.HIGH
      if (hasClosedReedSwitch) {
        expectedUpdates.add(0, "CLOSED")
      }
    }
    gateDevice.init()

    when:
    if (reedSwitchChange == "opening") {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH
    } else if (reedSwitchChange == "closing") {
      openReedSwitchBinaryInput.state = BinaryState.HIGH
    }

    then:
    listenerStub.receivedUpdates*.newValue == expectedUpdates

    where:
    initialState | expectedUpdates | hasClosedReedSwitch | hasOpenReedSwitch | reedSwitchChange | whenDescription
    "CLOSED"     | ["OPENING"]     | true                | true              | "opening"        | "opening without action (both reed switches)"
    "CLOSED"     | ["OPEN"]        | true                | false             | "opening"        | "opening without action (closed reed switch only)"
    "OPEN"       | ["CLOSING"]     | true                | true              | "closing"        | "closing without action (both reed switches)"
    "OPEN"       | ["CLOSED"]      | false               | true              | "closing"        | "closing without action (open reed switch only)"
  }

}
