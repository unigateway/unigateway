package com.mqgateway.core.device

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class SwitchButtonDeviceTest extends Specification {

	public static final int LONG_PRESS_MS = 100
	def pin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)

	def conditions = new PollingConditions(initialDelay: 0.1, timeout: 1)

	@Subject
	SwitchButtonDevice device = new SwitchButtonDevice("button1", pin, 0, LONG_PRESS_MS)

	def "should set debounce on pin during initialization"() {
    given:
    SwitchButtonDevice deviceWithDebounce = new SwitchButtonDevice("button1", pin, 200, LONG_PRESS_MS)

		when:
    deviceWithDebounce.init()

		then:
		pin.getDebounce(PinState.HIGH) == 200
	}

	def "should notify listeners on switch button pressed (LOW state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()

		when:
		pin.setState(PinState.LOW)

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("button1", "state", "PRESSED")
	}

	def "should notify listeners on switch button released (HIGH state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
    pin.setState(PinState.LOW)

		when:
		pin.setState(PinState.HIGH)

		then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("button1", "state", "RELEASED")
	}

	def "should notify listeners on switch button long pressed"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()

		when:
		pin.low()

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.find {it.newValue == "LONG_PRESSED" }
		}
	}

	def "should notify listeners on switch button long released"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
		pin.low()
		conditions.eventually {
			assert listenerStub.receivedUpdates.find {it.newValue == "LONG_PRESSED" }
		}

		when:
		pin.high()

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.find {it.newValue == "LONG_RELEASED" }
		}
	}
}
