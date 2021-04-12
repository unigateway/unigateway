package com.mqgateway.core.device

import static com.mqgateway.core.device.EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE
import static com.mqgateway.core.device.EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class SingleButtonsGateDeviceTest extends Specification {

  def actionPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
  EmulatedSwitchButtonDevice actionButton = new EmulatedSwitchButtonDevice("actionButton", actionPin, 10)

  def openReedSwitchPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
  ReedSwitchDevice openReedSwitch = new ReedSwitchDevice("openReedSwitch", openReedSwitchPin, 0)
  def closedReedSwitchPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
  ReedSwitchDevice closedReedSwitch = new ReedSwitchDevice("closedReedSwitch", closedReedSwitchPin, 0)

  @Subject
  SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, openReedSwitch, closedReedSwitch)

  UpdateListenerStub listenerStub = new UpdateListenerStub()

  def conditions = new PollingConditions(delay: 0.01)

  def "should activate actionButton emulated switch on state changed to OPEN when gate was closed"() {
    given:
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    closedReedSwitchPin.state = PinState.LOW // Gate closed
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
    openReedSwitchPin.state = PinState.LOW // Gate open
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
      closedReedSwitchPin.state = PinState.LOW // Gate closed
    } else if (oldState == 'OPEN') {
      openReedSwitchPin.state = PinState.LOW // Gate open
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
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, null, closedReedSwitch)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    closedReedSwitchPin.state = PinState.LOW // Gate closed
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
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton, openReedSwitch, null)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    openReedSwitchPin.state = PinState.LOW // Gate open
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
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton,
                                                                     hasOpenReedSwitch ? openReedSwitch : null,
                                                                     hasClosedReedSwitch ? closedReedSwitch : null)
    actionButton.addListener(listenerStub)
    gateDevice.addListener(new UpdateListenerStub())
    List<String> expectedUpdates = []
    if (initialState == 'OPEN') {
      closedReedSwitchPin.state = PinState.HIGH
      openReedSwitchPin.state = PinState.LOW // Gate open
    } else if (initialState == 'CLOSED') {
      closedReedSwitchPin.state = PinState.LOW // Gate closed
      openReedSwitchPin.state = PinState.HIGH
    } else if (initialState == 'OPENING') {
      closedReedSwitchPin.state = PinState.HIGH // Gate position unknown
      openReedSwitchPin.state = PinState.HIGH // Gate position unknown
      gateDevice.open()
      expectedUpdates.addAll([
        PRESSED_STATE_VALUE, // first time is for OPEN send above
        RELEASED_STATE_VALUE
      ])
    } else if (initialState == 'CLOSING') {
      closedReedSwitchPin.state = PinState.HIGH // Gate position unknown
      openReedSwitchPin.state = PinState.HIGH // Gate position unknown
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
    SingleButtonsGateDevice gateDevice = new SingleButtonsGateDevice("testGate", actionButton,
                                                                     hasOpenReedSwitch ? openReedSwitch : null,
                                                                     hasClosedReedSwitch ? closedReedSwitch : null)
    gateDevice.addListener(listenerStub)
    if (initialState == 'OPEN') {
      closedReedSwitchPin.state = PinState.HIGH
      openReedSwitchPin.state = PinState.LOW // Gate open
      if (hasOpenReedSwitch) {
        expectedUpdates.add(0, "OPEN")
      }
    } else if (initialState == 'CLOSED') {
      closedReedSwitchPin.state = PinState.LOW // Gate closed
      openReedSwitchPin.state = PinState.HIGH
      if (hasClosedReedSwitch) {
        expectedUpdates.add(0, "CLOSED")
      }
    } else if (initialState == 'OPENING') {
      closedReedSwitchPin.state = PinState.HIGH // Gate position unknown
      openReedSwitchPin.state = PinState.HIGH // Gate position unknown
      gateDevice.open()
      expectedUpdates.add(0, "OPENING")
    } else if (initialState == 'CLOSING') {
      closedReedSwitchPin.state = PinState.HIGH // Gate position unknown
      openReedSwitchPin.state = PinState.HIGH // Gate position unknown
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
}
