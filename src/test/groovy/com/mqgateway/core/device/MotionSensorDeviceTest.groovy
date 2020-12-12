package com.mqgateway.core.device

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import spock.lang.Specification
import spock.lang.Subject

class MotionSensorDeviceTest extends Specification {

	def pin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)

	@Subject
	MotionSensorDevice device = new MotionSensorDevice("device1", pin, 0)

  void setup() {
    // when sensor will be connected it will keep LOW state when no motion
    pin.setState(PinState.LOW)
  }

  def "should set debounce on pin during initialization"() {
    given:
    MotionSensorDevice deviceWithDebounce = new MotionSensorDevice("device1", pin, 300)

    when:
		deviceWithDebounce.init()

		then:
		pin.getDebounce(PinState.HIGH) == 300
	}

	def "should notify listeners on motion (HIGH state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()

		when:
		pin.setState(PinState.HIGH)

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("device1", "state", "ON")
	}

	def "should notify listeners on motion stopped (LOW state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
    pin.setState(PinState.HIGH)

		when:
		pin.setState(PinState.LOW)

		then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("device1", "state", "OFF")
	}
}
