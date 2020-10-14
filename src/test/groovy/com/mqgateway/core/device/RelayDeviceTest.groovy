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
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class RelayDeviceTest extends Specification {

	def pinImpl = new PinImpl("com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", 150, "com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", EnumSet<PinMode>.of(PinMode.DIGITAL_OUTPUT))
	def gpioProvider = new SimulatedGpioProvider()
	def pin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pinImpl)

	@Subject
	RelayDevice relay = new RelayDevice("relay1", pin)

	void setup() {
		gpioProvider.setMode(pinImpl, PinMode.DIGITAL_OUTPUT)
	}

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
		def conditions = new PollingConditions()

		when:
		relay.change("state", "ON")

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", "ON")
		}
	}

	def "should notify listeners on relay opened - OFF"() {
		given:
		def listenerStub = new UpdateListenerStub()
		relay.addListener(listenerStub)
		relay.init()
		def conditions = new PollingConditions()

		when:
		relay.change("state", "OFF")

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", "OFF")
		}
	}
}
