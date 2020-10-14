package com.mqgateway.core.device

import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.SimulatedGpioProvider
import com.pi4j.io.gpio.impl.GpioControllerImpl
import com.pi4j.io.gpio.impl.GpioPinImpl
import com.pi4j.io.gpio.impl.PinImpl
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class EmulatedSwitchButtonDeviceTest extends Specification {
	def pinImpl = new PinImpl("com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", 150, "com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", EnumSet<PinMode>.of(PinMode.DIGITAL_OUTPUT))
	def gpioProvider = new SimulatedGpioProvider()
	def pin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pinImpl)

	@Subject
	EmulatedSwitchButtonDevice emulatedSwitch = new EmulatedSwitchButtonDevice("emulatedSwitch1", pin)

	void setup() {
		gpioProvider.setMode(pinImpl, PinMode.DIGITAL_OUTPUT)
	}

	@Unroll
	def "should change pin state to LOW when requested to be PRESSED"() {
		when:
		emulatedSwitch.change("state", "PRESSED")

		then:
		pin.getState() == PinState.LOW
	}

	def "should notify listeners on button pressed"() {
		given:
		def listenerStub = new UpdateListenerStub()
		emulatedSwitch.addListener(listenerStub)
		emulatedSwitch.init()
		def conditions = new PollingConditions()

		when:
		emulatedSwitch.change("state", "PRESSED")

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("emulatedSwitch1", "state", "PRESSED")
		}
	}

	def "should release button shortly after it was requested to be PRESSED"() {
		given:
		def listenerStub = new UpdateListenerStub()
		emulatedSwitch.addListener(listenerStub)
		emulatedSwitch.init()
		def conditions = new PollingConditions()

		when:
		emulatedSwitch.change("state", "PRESSED")

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates[1] == new UpdateListenerStub.Update("emulatedSwitch1", "state", "RELEASED")
		}
	}
}
