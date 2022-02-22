package com.mqgateway.core.device

import com.mqgateway.core.hardware.simulated.SimulatedBinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class EmulatedSwitchButtonDeviceTest extends Specification {
	SimulatedBinaryOutput binaryOutput = new SimulatedBinaryOutput()

	@Subject
	EmulatedSwitchButtonDevice emulatedSwitch = new EmulatedSwitchButtonDevice("emulatedSwitch1", binaryOutput, 10)

	@Unroll
	def "should change pin state to LOW when requested to be PRESSED"() {
		when:
		emulatedSwitch.change("state", "PRESSED")

		then:
		binaryOutput.getState() == BinaryState.LOW
	}

	def "should notify listeners on button pressed"() {
		given:
		def listenerStub = new UpdateListenerStub()
		emulatedSwitch.addListener(listenerStub)
		emulatedSwitch.init()

		when:
		emulatedSwitch.change("state", "PRESSED")

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("emulatedSwitch1", "state", "PRESSED")
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
