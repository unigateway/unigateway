package com.mqgateway.core.device.gate

import static com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE
import static com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE

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

class SingleButtonsGateDeviceTest extends Specification {

  def actionBinaryOutput = new SimulatedBinaryOutput()
  EmulatedSwitchButtonDevice actionButton = new EmulatedSwitchButtonDevice("actionButton", actionBinaryOutput, 10)

  def openReedSwitchBinaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
	ReedSwitchDevice openReedSwitch = new ReedSwitchDevice("openReedSwitch", openReedSwitchBinaryInput)
  def closedReedSwitchBinaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
  ReedSwitchDevice closedReedSwitch = new ReedSwitchDevice("closedReedSwitch", closedReedSwitchBinaryInput)

  @Subject
  SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, openReedSwitch, closedReedSwitch)

  UpdateListenerStub listenerStub = new UpdateListenerStub()

  def conditions = new PollingConditions(delay: 0.01)

  def "should activate actionButton emulated switch on state changed to OPEN when gate was closed"() {
    given:
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "OPEN")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE)
      ]
    }
  }

  def "should activate actionButton emulated switch on state changed to CLOSE when gate was open"() {
    given:
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), "CLOSE")

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE)
      ]
    }
  }

  @Unroll
  def "should ignore status change '#statusChange' when gate was already in state '#oldState' and both reed switches are installed"() {
    given:
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    if (oldState == 'CLOSED') {
      closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
    } else if (oldState == 'OPEN') {
      openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
    }
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), statusChange)

    then:
    listenerStub.receivedUpdates == []

    where:
    oldState || statusChange
    'CLOSED' || 'CLOSE'
    'OPEN'   || 'OPEN'
    'CLOSED' || 'STOP'
    'OPEN'   || 'STOP'
  }

  def "should trigger action button on state change OPEN when there is no reed switch for open state and even though gate is in OPEN state"() {
    given:
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, null, closedReedSwitch)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), 'OPEN')
    gateDevice.change(STATE.toString(), 'OPEN')

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.containsAll([
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE)
      ])
    }
  }

  def "should trigger action button on state change CLOSE when there is no reed switch for closed state and even though gate is in CLOSED state"() {
    given:
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, openReedSwitch, null)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), 'CLOSE')
    gateDevice.change(STATE.toString(), 'CLOSE')

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.containsAll([
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE)
      ])
    }
  }

  def "should trigger action button on state change STOP in any state when there is no reed switch"() {
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, null, null)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    if (initialState == 'CLOSED') {
      gateDevice.close()
    } else if (initialState == 'OPEN') {
      gateDevice.open()
    }
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), 'STOP')

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates == [
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE), // first time is for OPEN/CLOSE
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE),
        new UpdateListenerStub.Update("actionButton", STATE.toString(), PRESSED_STATE_VALUE), // second time is for STOP
        new UpdateListenerStub.Update("actionButton", STATE.toString(), RELEASED_STATE_VALUE)
      ]
    }

    where:
    initialState << ["CLOSED", "OPEN"]
  }

  def "should trigger action button on state change STOP only if gate state is not closed when there is only closedReedSwitch available"() {
    given:
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton,
                                                                     hasOpenReedSwitch ? openReedSwitch : null,
                                                                     hasClosedReedSwitch ? closedReedSwitch : null)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    List<String> expectedUpdates = []
    if (initialState == 'OPEN') {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH
      openReedSwitchBinaryInput.state = BinaryState.LOW // Gate open
    } else if (initialState == 'CLOSED') {
      closedReedSwitchBinaryInput.state = BinaryState.LOW // Gate closed
      openReedSwitchBinaryInput.state = BinaryState.HIGH
    } else if (initialState == 'OPENING') {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      openReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      gateDevice.open()
      expectedUpdates.addAll([
        PRESSED_STATE_VALUE, // first time is for OPEN send above
        RELEASED_STATE_VALUE
      ])
    } else if (initialState == 'CLOSING') {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      openReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      gateDevice.close()
      expectedUpdates.addAll([
        PRESSED_STATE_VALUE, // first time is for CLOSE send above
        RELEASED_STATE_VALUE
      ])
    }
    gateDevice.init()
    expectedUpdates.addAll(expectedButtonStates)

    when:
    gateDevice.change(STATE.toString(), action)

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.collect {it.newValue} == expectedUpdates
    }

    where:
    initialState | action | hasClosedReedSwitch | hasOpenReedSwitch | expectedButtonStates
    "OPEN"       | "STOP" | true                | false             | ["PRESSED", "RELEASED"]
    "OPEN"       | "STOP" | false               | true              | []
    "OPENING"    | "STOP" | false               | true              | ["PRESSED", "RELEASED"]
    "CLOSED"     | "STOP" | true                | false             | []
    "CLOSED"     | "STOP" | false               | true              | ["PRESSED", "RELEASED"]
    "CLOSING"    | "STOP" | true                | false             | ["PRESSED", "RELEASED"]
  }

  def "should gate state change from #initialState to #expectedUpdates when sent state change '#action' and #whenDescription"() {
    given:
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton,
                                                                     hasOpenReedSwitch ? openReedSwitch : null,
                                                                     hasClosedReedSwitch ? closedReedSwitch : null)
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
    } else if (initialState == 'OPENING') {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      openReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      gateDevice.open()
      expectedUpdates.add(0, "OPENING")
    } else if (initialState == 'CLOSING') {
      closedReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      openReedSwitchBinaryInput.state = BinaryState.HIGH // Gate position unknown
      gateDevice.close()
      expectedUpdates.add(0, "CLOSING")
    }
    gateDevice.init()

    when:
    gateDevice.change(STATE.toString(), action)

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.collect { it.newValue } == expectedUpdates
    }

    where:
    initialState | action  | expectedUpdates     | hasClosedReedSwitch | hasOpenReedSwitch | whenDescription
    "OPEN"       | "CLOSE" | ["CLOSING"]         | true                | false             | "closedReedSwitch is available"
    "OPEN"       | "OPEN"  | ["OPEN"]            | true                | false             | "closedReedSwitch is available"
    "OPEN"       | "STOP"  | ["OPEN"]            | true                | false             | "closedReedSwitch is available"
    "CLOSED"     | "CLOSE" | []                  | true                | false             | "closedReedSwitch is available"
    "CLOSED"     | "OPEN"  | ["OPEN"]            | true                | false             | "closedReedSwitch is available"
    "CLOSED"     | "STOP"  | []                  | true                | false             | "closedReedSwitch is available"
    "CLOSING"    | "CLOSE" | []                  | true                | false             | "closedReedSwitch is available"
    "CLOSING"    | "OPEN"  | ["OPEN", "OPEN"]    | true                | false             | "closedReedSwitch is available"
    "CLOSING"    | "STOP"  | ["OPEN"]            | true                | false             | "closedReedSwitch is available"
    "OPEN"       | "CLOSE" | ["CLOSED"]          | false               | true              | "openReedSwitch is available"
    "OPEN"       | "OPEN"  | []                  | false               | true              | "openReedSwitch is available"
    "OPEN"       | "STOP"  | []                  | false               | true              | "openReedSwitch is available"
    "CLOSED"     | "CLOSE" | ["CLOSED"]          | false               | true              | "openReedSwitch is available"
    "CLOSED"     | "OPEN"  | ["OPENING"]         | false               | true              | "openReedSwitch is available"
    "CLOSED"     | "STOP"  | ["OPEN"]            | false               | true              | "openReedSwitch is available"
    "OPENING"    | "CLOSE" | ["OPEN", "CLOSED"]  | false               | true              | "openReedSwitch is available"
    "OPENING"    | "OPEN"  | []                  | false               | true              | "openReedSwitch is available"
    "OPENING"    | "STOP"  | ["OPEN"]            | false               | true              | "openReedSwitch is available"
    "OPEN"       | "CLOSE" | ["CLOSING"]         | true                | true              | "both reed switches are available"
    "OPEN"       | "OPEN"  | []                  | true                | true              | "both reed switches are available"
    "OPEN"       | "STOP"  | []                  | true                | true              | "both reed switches are available"
    "CLOSED"     | "CLOSE" | []                  | true                | true              | "both reed switches are available"
    "CLOSED"     | "OPEN"  | ["OPENING"]         | true                | true              | "both reed switches are available"
    "CLOSED"     | "STOP"  | []                  | true                | true              | "both reed switches are available"
    "OPENING"    | "CLOSE" | ["OPEN", "CLOSING"] | true                | true              | "both reed switches are available"
    "OPENING"    | "OPEN"  | []                  | true                | true              | "both reed switches are available"
    "OPENING"    | "STOP"  | ["OPEN"]            | true                | true              | "both reed switches are available"
    "CLOSING"    | "CLOSE" | []                  | true                | true              | "both reed switches are available"
    "CLOSING"    | "OPEN"  | ["OPEN", "OPENING"] | true                | true              | "both reed switches are available"
    "CLOSING"    | "STOP"  | ["OPEN"]            | true                | true              | "both reed switches are available"
    "OPEN"       | "CLOSE" | ["CLOSED"]          | false               | false             | "none reed switch is available"
    "OPEN"       | "OPEN"  | ["OPEN"]            | false               | false             | "none reed switch is available"
    "OPEN"       | "STOP"  | ["OPEN"]            | false               | false             | "none reed switch is available"
    "CLOSED"     | "CLOSE" | ["CLOSED"]          | false               | false             | "none reed switch is available"
    "CLOSED"     | "OPEN"  | ["OPEN"]            | false               | false             | "none reed switch is available"
    "CLOSED"     | "STOP"  | ["OPEN"]            | false               | false             | "none reed switch is available"
  }

  @Unroll
  def "should change gate state when reed switch reports #whenDescription"() {
    given:
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton,
                                                                     hasOpenReedSwitch ? openReedSwitch : null,
                                                                     hasClosedReedSwitch ? closedReedSwitch : null)
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
    initialState | expectedUpdates     | hasClosedReedSwitch | hasOpenReedSwitch | reedSwitchChange | whenDescription
    "CLOSED"     | ["OPENING"]         | true                | true              | "opening" | "opening without action (both reed switches)"
    "CLOSED"     | ["OPEN"]            | true                | false             | "opening" | "opening without action (closed reed switch only)"
    "OPEN"       | ["CLOSING"]         | true                | true              | "closing" | "closing without action (both reed switches)"
    "OPEN"       | ["CLOSED"]          | false               | true              | "closing" | "closing without action (open reed switch only)"
  }
}
