package com.mqgateway.core.device

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject

class ReedSwitchDeviceTest extends Specification {

	def pin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)

	@Subject
	ReedSwitchDevice device = new ReedSwitchDevice("reed1", pin, 0)

  void setup() {
    // starting with closed circuit (LOW state)
    pin.setState(PinState.LOW)
  }

  def "should set debounce on pin during initialization"() {
    given:
    ReedSwitchDevice deviceWithDebounce = new ReedSwitchDevice("reed1", pin, 150)

		when:
    deviceWithDebounce.init()

		then:
		pin.getDebounce(PinState.HIGH) == 150
	}

	def "should notify listeners on reed switch open (HIGH state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()

		when:
		pin.setState(PinState.HIGH)

		then:
		listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("reed1", "state", "OPEN")
	}

	def "should notify listeners on reed switch closed (LOW state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
    pin.setState(PinState.HIGH)

		when:
		pin.setState(PinState.LOW)

		then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("reed1", "state", "CLOSED")
	}
}
