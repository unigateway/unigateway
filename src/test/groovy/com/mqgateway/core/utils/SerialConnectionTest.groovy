package com.mqgateway.core.utils

import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.core.hardware.MqGpioPinDigitalStateChangeEvent
import com.mqgateway.core.hardware.MqGpioPinListenerDigital
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.pi4j.io.gpio.PinState
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class SerialConnectionTest extends Specification {

  SimulatedSerial serialStub = new SimulatedSerial()

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
		SimulatedGpioPinDigitalOutput askForDataPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
		TestSerialDataListener listener = new TestSerialDataListener(id, askForDataPin)
		askForDataPin.addListener(new MqGpioPinListenerDigital() {
      @Override
      void handleGpioPinDigitalStateChangeEvent(@NotNull MqGpioPinDigitalStateChangeEvent event) {
        if (event.state == PinState.LOW) {
          serialStub.sendFakeMessage(messageToBeReceived)
        }
      }
    })
		return listener
	}

	def "should throw exception if asked for data but not initialized before"() {
		given:
		SerialConnection serialConnection = new SerialConnection(serialStub, 500)
		TestSerialDataListener listener = new TestSerialDataListener("testId", new SimulatedGpioPinDigitalOutput(PinState.HIGH))

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
		SimulatedGpioPinDigitalOutput askForDataPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
		TestSerialDataListener listener = new TestSerialDataListener("testId1", askForDataPin)
		askForDataPin.addListener(new MqGpioPinListenerDigital() {
      @Override
      void handleGpioPinDigitalStateChangeEvent(@NotNull MqGpioPinDigitalStateChangeEvent event) {
        // do nothing
      }
    })

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
		SimulatedGpioPinDigitalOutput askForDataPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)
		TestSerialDataListener listener = new TestSerialDataListener("testId1", askForDataPin)
    askForDataPin.addListener(new MqGpioPinListenerDigital() {
      @Override
      void handleGpioPinDigitalStateChangeEvent(@NotNull MqGpioPinDigitalStateChangeEvent event) {
        serialStub.sendFakeMessage("foobar")
      }
    })

		when:
		serialConnection.askForData(listener)
		serialConnection.getDataForAllListeners()

		then:
		askForDataPin.state == PinState.HIGH
	}
}

class TestSerialDataListener implements SerialDataListener {

	String id
	List<String> receivedMessages = []
	boolean dataReceived = false

	private SimulatedGpioPinDigitalOutput askForDataPin

	TestSerialDataListener(String id, SimulatedGpioPinDigitalOutput askForDataPin) {
		this.id = id
		this.askForDataPin = askForDataPin
	}

	@Override
	String id() {
		return id
	}

	@Override
  MqGpioPinDigitalOutput askForDataPin() {
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
