package com.mqgateway.core.device

import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.SimulatedGpioProvider
import com.pi4j.io.gpio.impl.GpioControllerImpl
import com.pi4j.io.gpio.impl.GpioPinImpl
import com.pi4j.io.gpio.impl.PinImpl
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class SwitchButtonDeviceTest extends Specification {

	public static final int LONG_PRESS_MS = 100
	def pinImpl = new PinImpl("com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", 150, "", EnumSet<PinMode>.of(PinMode.DIGITAL_INPUT))
	def gpioProvider = new SimulatedGpioProvider()
	def pin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pinImpl)

	def conditions = new PollingConditions(initialDelay: 0.1, timeout: 1)

	@Subject
	SwitchButtonDevice device = new SwitchButtonDevice("button1", pin, 200, LONG_PRESS_MS)

	void setup() {
		pin.setMode(PinMode.DIGITAL_INPUT)
	}

	def "should set debounce on pin during initialization"() {
		when:
		device.init()

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
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("button1", "state", "PRESSED", null)
		}
	}

	def "should notify listeners on switch button released (HIGH state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()

		when:
		pin.setState(PinState.HIGH)

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("button1", "state", "RELEASED", null)
		}
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
