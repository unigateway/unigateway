package com.mqgateway.core.device

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RelayDeviceTest extends Specification {

	def pin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)

	@Subject
	RelayDevice relay = new RelayDevice("relay1", pin)

	@Unroll
	def "should change pin state when requested to #newState"(String newState, PinState pinState) {
		when:
		relay.change("state", newState)

		then:
		pin.getState() == pinState

		where:
		newState || pinState
		"OFF"    || PinState.HIGH
		"ON"     || PinState.LOW
	}

	def "should notify listeners on relay closed - ON"() {
		given:
		def listenerStub = new UpdateListenerStub()
		relay.addListener(listenerStub)
		relay.init()

		when:
		relay.change("state", "ON")

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", "ON")
	}

	def "should notify listeners on relay opened - OFF"() {
		given:
		def listenerStub = new UpdateListenerStub()
		relay.addListener(listenerStub)
		relay.init()

		when:
		relay.change("state", "OFF")

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", "OFF")
	}
}
