package com.mqgateway.core.device

import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.SimulatedGpioProvider
import com.pi4j.io.gpio.impl.GpioControllerImpl
import com.pi4j.io.gpio.impl.GpioPinImpl
import com.pi4j.io.gpio.impl.PinImpl
import java.time.LocalDateTime
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class TimerSwitchRelayDeviceTest extends Specification {
	def pinImpl = new PinImpl("com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", 150, "com.pi4j.gpio.extension.mcp.MCP23017GpioProvider", EnumSet<PinMode>.of(PinMode.DIGITAL_OUTPUT))
	def gpioProvider = new SimulatedGpioProvider()
	def pin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pinImpl)
	def timerScheduler = new TimersScheduler()

	@Subject
	TimerSwitchRelayDevice timerSwitch = new TimerSwitchRelayDevice("timerSwitch1", pin, timerScheduler)

	void setup() {
		gpioProvider.setMode(pinImpl, PinMode.DIGITAL_OUTPUT)
	}

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
		def conditions = new PollingConditions()

		when:
		timerSwitch.change("timer", "10")

		then:
		conditions.eventually {
			def stateUpdate = listenerStub.receivedUpdates.find { it.propertyId == "state" }
			assert stateUpdate == new UpdateListenerStub.Update("timerSwitch1", "state", "ON", null)
		}
	}

	def "should notify listeners on switch turned OFF after time has finished"() {
		given:
		def listenerStub = new UpdateListenerStub()
		timerSwitch.addListener(listenerStub)
		timerSwitch.init()
		def conditions = new PollingConditions()
		timerSwitch.change("timer", "10")

		when:
		timerSwitch.updateTimer(LocalDateTime.now().plusMinutes(10).plusSeconds(1))

		then:
		conditions.eventually {
			def stateUpdate = listenerStub.receivedUpdates.findAll { it.propertyId == "state" }.last()
			assert stateUpdate == new UpdateListenerStub.Update("timerSwitch1", "state", "OFF", null)
		}
	}

	def "should notify listeners on switch turned OFF when 'timer' property set to zero"() {
		given:
		def listenerStub = new UpdateListenerStub()
		timerSwitch.addListener(listenerStub)
		timerSwitch.init()
		def conditions = new PollingConditions()
		timerSwitch.change("timer", "10")

		when:
		timerSwitch.change("timer", "0")

		then:
		conditions.eventually {
			def stateUpdate = listenerStub.receivedUpdates.findAll { it.propertyId == "state" }.last()
			assert stateUpdate == new UpdateListenerStub.Update("timerSwitch1", "state", "OFF", null)
		}
	}
}
