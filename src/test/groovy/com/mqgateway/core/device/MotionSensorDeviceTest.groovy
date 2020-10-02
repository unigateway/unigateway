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

class MotionSensorDeviceTest extends Specification {

	def pinImpl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_INPUT))
	def gpioProvider = new SimulatedGpioProvider()
	def pin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pinImpl)

	@Subject
	MotionSensorDevice device = new MotionSensorDevice("device1", pin, 300)

	def "should set debounce on pin during initialization"() {
		when:
		device.init()

		then:
		pin.getDebounce(PinState.HIGH) == 300
	}

	def "should notify listeners on motion (HIGH state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
		def conditions = new PollingConditions()

		when:
		pin.setState(PinState.HIGH)

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("device1", "state", "ON", null)
		}
	}

	def "should notify listeners on motion stopped started (LOW state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
		def conditions = new PollingConditions()

		when:
		pin.setState(PinState.LOW)

		then:
		conditions.eventually {
			assert listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("device1", "state", "OFF", null)
		}
	}
}
