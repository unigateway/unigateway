package com.mqgateway.core.device

import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinState
import java.time.LocalDateTime
import spock.lang.Specification
import spock.lang.Subject

class TimerSwitchRelayDeviceTest extends Specification {
	def pin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
	def timerScheduler = new TimersScheduler()

	@Subject
	TimerSwitchRelayDevice timerSwitch = new TimerSwitchRelayDevice("timerSwitch1", pin, timerScheduler)

	def "should turn on relay when timer is set"() {
		when:
		timerSwitch.change("timer", "10")

		then:
		pin.getState() == PinState.LOW
	}


	def "should turn off relay when timer is set to zero"() {
		given:
		timerSwitch.change("timer", "10")

		when:
		timerSwitch.change("timer", "0")

		then:
		pin.getState() == PinState.HIGH
	}

	def "should turn off relay when time is up"() {
		given:
		timerSwitch.change("timer", "1")

		when:
		timerSwitch.updateTimer(LocalDateTime.now().plusMinutes(1))

		then:
		pin.getState() == PinState.HIGH
	}

	def "should notify listeners on switch turned ON"() {
		given:
		def listenerStub = new UpdateListenerStub()
		timerSwitch.addListener(listenerStub)
		timerSwitch.init()

		when:
		timerSwitch.change("timer", "10")

		then:
    def stateUpdate = listenerStub.receivedUpdates.find { it.propertyId == "state" }
    stateUpdate == new UpdateListenerStub.Update("timerSwitch1", "state", "ON")
	}

	def "should notify listeners on switch turned OFF after time has finished"() {
		given:
		def listenerStub = new UpdateListenerStub()
		timerSwitch.addListener(listenerStub)
		timerSwitch.init()
		timerSwitch.change("timer", "10")

		when:
		timerSwitch.updateTimer(LocalDateTime.now().plusMinutes(10).plusSeconds(1))

		then:
		def stateUpdate = listenerStub.receivedUpdates.findAll { it.propertyId == "state" }.last()
    stateUpdate == new UpdateListenerStub.Update("timerSwitch1", "state", "OFF")
	}

	def "should notify listeners on switch turned OFF when 'timer' property set to zero"() {
		given:
		def listenerStub = new UpdateListenerStub()
		timerSwitch.addListener(listenerStub)
		timerSwitch.init()
		timerSwitch.change("timer", "10")

		when:
		timerSwitch.change("timer", "0")

		then:
    def stateUpdate = listenerStub.receivedUpdates.findAll { it.propertyId == "state" }.last()
    stateUpdate == new UpdateListenerStub.Update("timerSwitch1", "state", "OFF")
	}
}
