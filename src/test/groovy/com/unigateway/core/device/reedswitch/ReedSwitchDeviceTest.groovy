package com.unigateway.core.device.reedswitch

import com.unigateway.core.device.reedswitch.ReedSwitchDevice
import com.unigateway.core.hardware.simulated.SimulatedBinaryInput
import com.unigateway.core.io.BinaryState
import com.unigateway.utils.UpdateListenerStub
import com.unigateway.core.hardware.simulated.SimulatedBinaryInput
import com.unigateway.core.io.BinaryState
import com.unigateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject

class ReedSwitchDeviceTest extends Specification {

	SimulatedBinaryInput binaryInput = new SimulatedBinaryInput(BinaryState.LOW) // starting with closed circuit (LOW state)

	@Subject
	ReedSwitchDevice device = new ReedSwitchDevice("reed1", "Reed switch", binaryInput, [:])

	def "should notify listeners on reed switch open (HIGH state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()

		when:
		binaryInput.high()

		then:
		listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("reed1", "state", "OPEN")
	}

	def "should notify listeners on reed switch closed (LOW state)"() {
		given:
		def listenerStub = new UpdateListenerStub()
		device.addListener(listenerStub)
		device.init()
    binaryInput.high()

		when:
		binaryInput.low()

		then:
    listenerStub.receivedUpdates.last() == new UpdateListenerStub.Update("reed1", "state", "CLOSED")
	}
}
