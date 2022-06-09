package com.unigateway.core.device.relay

import com.unigateway.core.hardware.simulated.SimulatedBinaryOutput
import com.unigateway.core.io.BinaryState
import com.unigateway.utils.UpdateListenerStub
import com.unigateway.core.hardware.simulated.SimulatedBinaryOutput
import com.unigateway.core.io.BinaryState
import com.unigateway.utils.UpdateListenerStub
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RelayDeviceTest extends Specification {

  SimulatedBinaryOutput binaryOutput = new SimulatedBinaryOutput()

	@Subject
	RelayDevice relay = new RelayDevice("relay1", "Relay device", binaryOutput, BinaryState.LOW, [:])

	@Unroll
	def "should change pin state when requested to #newState"(String newState, BinaryState binaryState) {
		when:
		relay.change("state", newState)

		then:
		binaryOutput.getState() == binaryState

		where:
		newState || binaryState
		"OFF"    || BinaryState.HIGH
		"ON"     || BinaryState.LOW
	}

	def "should notify listeners on relay closed - ON"() {
		given:
		def listenerStub = new UpdateListenerStub()
		relay.addListener(listenerStub)
		relay.init()

		when:
		relay.change("state", "ON")

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", "ON")
	}

	def "should notify listeners on relay opened - OFF"() {
		given:
		def listenerStub = new UpdateListenerStub()
		relay.addListener(listenerStub)
		relay.init()

		when:
		relay.change("state", "OFF")

		then:
    listenerStub.receivedUpdates.first() == new UpdateListenerStub.Update("relay1", "state", "OFF")
	}

  @Unroll
  def "should change pin state when requested to #newState when trigger level is set to LOW"(String newState, BinaryState binaryState) {
    when:
    RelayDevice relayHigh = new RelayDevice("relay1", "Relay device", binaryOutput, BinaryState.HIGH, [:])
    relayHigh.change("state", newState)

    then:
    binaryOutput.getState() == binaryState

    where:
    newState || binaryState
    "OFF"    || BinaryState.LOW
    "ON"     || BinaryState.HIGH
  }
}
