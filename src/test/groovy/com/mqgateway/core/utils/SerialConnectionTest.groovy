package com.mqgateway.core.utils

import com.mqgateway.utils.SerialStub
import com.pi4j.io.gpio.GpioPinDigitalOutput
import org.jetbrains.annotations.Nullable
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class SerialConnectionTest extends Specification {

	SerialStub serialStub = new SerialStub()

	@Subject
	SerialConnection serialConnection = new SerialConnection(serialStub, 50)

	PollingConditions conditions = new PollingConditions(timeout: 5)

	void setup() {
		serialConnection.init()
	}

	def "should retrieve data for all registered listeners"() {
		given:
		String testMessage = "Some message"
		List<TestSerialDataListener> listeners = (1..10).collect { prepareListener(it.toString(), testMessage + " $it")}

		when:
		listeners.forEach {listener -> serialConnection.askForData(listener) }
		serialConnection.getDataForAllListeners()

		then:
		listeners.forEach {
			it.receivedMessage == testMessage + " " + it.id()
		}
	}

	private TestSerialDataListener prepareListener(String id, String messageToBeReceived) {
		GpioPinDigitalOutput askForDataPin = Mock(GpioPinDigitalOutput)
		TestSerialDataListener listener = new TestSerialDataListener(id, askForDataPin)
		askForDataPin.low() >> {
			serialStub.sendFakeMessage(messageToBeReceived)
		}
		return listener
	}

	def "should throw exception if asked for data but not initialized before"() {
		given:
		SerialConnection serialConnection = new SerialConnection(serialStub, 500)
		TestSerialDataListener listener = new TestSerialDataListener("testId", Mock(GpioPinDigitalOutput))

		when:
		serialConnection.askForData(listener)

		then:
		thrown(SerialConnectionNotInitializedException)
	}

	def "should throw when trying to initialize twice"() {
		when:
		serialConnection.init()

		then:
		thrown(SerialConnectionAlreadyInitializedException)
	}

	def "should return null as message when it could not have been received within the specified time"() {
		given:
		GpioPinDigitalOutput askForDataPin = Mock(GpioPinDigitalOutput)
		TestSerialDataListener listener = new TestSerialDataListener("testId1", askForDataPin)
		askForDataPin.low() >> {
			// do nothing
		}

		when:
		serialConnection.askForData(listener)
		serialConnection.getDataForAllListeners()

		then:
		conditions.eventually {
			assert listener.dataReceived
			assert listener.receivedMessage == null
		}
	}

	def "should not process listener again after data has been received for it"() {
		given:
		def listener = prepareListener("testId2", "some message 2")

		when:
		serialConnection.askForData(listener)
		serialConnection.getDataForAllListeners()
		serialConnection.getDataForAllListeners()

		then:
		listener.receivedMessages.size() == 1
	}

	def "should put askForData pin back in HIGH state after message hes been received"() {
		given:
		def pinState = "NOT_SET"
		GpioPinDigitalOutput askForDataPin = Mock(GpioPinDigitalOutput)
		TestSerialDataListener listener = new TestSerialDataListener("testId1", askForDataPin)
		askForDataPin.low() >> {
			pinState = "LOW"
			serialStub.sendFakeMessage("foobar")
		}
		askForDataPin.high() >> {
			pinState = "HIGH"
		}

		when:
		serialConnection.askForData(listener)
		serialConnection.getDataForAllListeners()

		then:
		pinState == "HIGH"
	}
}

class TestSerialDataListener implements SerialDataListener {

	String id
	List<String> receivedMessages = []
	boolean dataReceived = false

	private GpioPinDigitalOutput askForDataPin

	TestSerialDataListener(String id, GpioPinDigitalOutput askForDataPin) {
		this.id = id
		this.askForDataPin = askForDataPin
	}

	@Override
	String id() {
		return id
	}

	@Override
	GpioPinDigitalOutput askForDataPin() {
		return askForDataPin
	}

	@Override
	void onDataReceived(@Nullable String message) {
		this.receivedMessages.add(message)
		dataReceived = true
	}

	String getReceivedMessage() {
		return receivedMessages[0]
	}
}
