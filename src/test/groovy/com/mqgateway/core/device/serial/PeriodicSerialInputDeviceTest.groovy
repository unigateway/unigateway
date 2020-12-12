package com.mqgateway.core.device.serial

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.utils.ExternalSerialDeviceSimulator
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import java.time.Duration
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class PeriodicSerialInputDeviceTest extends Specification {

	def inputPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
	def outputPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)

	TimersScheduler scheduler = new TimersScheduler()
  SimulatedSerial serialStub = new SimulatedSerial()
	SerialConnection serialConnection = new SerialConnection(serialStub, 5000L)
	UpdateListenerStub updateListenerStub = new UpdateListenerStub()

	@Subject
	PeriodicSerialInputDevice device = new SomePeriodicSerialInputDevice("someDevice", DeviceType.BME280, outputPin, inputPin, serialConnection,
																		 Duration.ofHours(1), Duration.ofSeconds(10), scheduler)

	PollingConditions conditions = new PollingConditions(timeout: 5)

	void setup() {
		serialConnection.init()
	}

	def "should set last_ping property on ping (HIGH state) from device"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		inputPin.high()

		when:
		inputPin.low()

		then:
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping"}
			update.newValue != null
		}
	}

	def "should receive data from external device when asking for it"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		fakeSerialDevice.setMessageToSend("Some test message 123456")
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping"}
			update.newValue != null
		}

		when:
		device.askForSerialDataNow()
		serialConnection.getDataForAllListeners()

		then:
		device.message == "Some test message 123456"
	}

	def "should not ask for data when there was no ping"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.setMessageToSend("Some test message 123456")

		when:
		device.askForSerialDataNow()
		serialConnection.getDataForAllListeners()

		then:
		device.message == null
	}

	def "should not throw exception if message not received"() {
		given:
		outputPin.low()
		SerialConnection serialConnection = new SerialConnection(serialStub, 50)
		PeriodicSerialInputDevice device = new SomePeriodicSerialInputDevice("someDevice", DeviceType.BME280, outputPin, inputPin, serialConnection,
																			 Duration.ofHours(1), Duration.ofSeconds(10), scheduler)
		device.addListener(updateListenerStub)
		device.init()
		serialConnection.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping"}
			assert update.newValue != null
		}

		when:
		device.askForSerialDataNow()
		serialConnection.getDataForAllListeners()

		then:
		notThrown()
	}

	def "should queue devices asking for data when multiple of them ask for data at the same time"() {
		given:
		def devicesElements = (1..10).collect {prepareSerialDevice("device$it")}
		devicesElements.each {
			def fakeSerialDevice = new ExternalSerialDeviceSimulator(it.outputPin, it.inputPin, serialStub)
			fakeSerialDevice.beforeSendingMessage {
				def random = new Random().nextInt(50)
				sleep(random)
			}
			fakeSerialDevice.ping()
			fakeSerialDevice.setMessageToSend("message for ${it.device.id}")
		}

		conditions.eventually {
			assert devicesElements.collect {it.device.id }.every {deviceId ->
				def update = updateListenerStub.receivedUpdates.find {it.deviceId == deviceId && it.propertyId == "last_ping" }
				update.newValue != null
			}
		}

		when:
		def devicesThreads = devicesElements.collect {deviceElement ->
			Thread.start {
				deviceElement.device.askForSerialDataNow()
			}
		}
		devicesThreads.each {it.join() }
		serialConnection.getDataForAllListeners()

		then:
		devicesElements.every {
			it.device.message == "message for ${it.device.id}"
		}
	}

	def "should not fail and do not pass message to implementation when external device returned error message"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		fakeSerialDevice.setMessageToSend("error;this is fake error message from tests")
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping"}
			update.newValue != null
		}

		when:
		device.askForSerialDataNow()
		serialConnection.getDataForAllListeners()

		then:
		device.message == null
	}

	def "should notify that device is OFFLINE when no ping has been received since acceptable ping period"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.setMessageToSend("Some test message 123456")

		when:
		device.askForSerialDataNow()
		serialConnection.getDataForAllListeners()

		then:
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "state"}
			update.newValue == "OFFLINE"
		}
	}

	def "should notify that device is ONLINE when ping has been received within acceptable ping period"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		fakeSerialDevice.setMessageToSend("Some test message 123456")
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping"}
			update.newValue != null
		}

		when:
		device.askForSerialDataNow()
		serialConnection.getDataForAllListeners()

		then:
		conditions.eventually {
			updateListenerStub.receivedUpdates.find {it.propertyId == "state" && it.newValue == "ONLINE"}
		}
	}

	DeviceElements prepareSerialDevice(String name) {
		def inputPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
		def outputPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)

		def device = new SomePeriodicSerialInputDevice(name, DeviceType.BME280, outputPin, inputPin, serialConnection,
														Duration.ofHours(1), Duration.ofSeconds(10), scheduler)
		device.addListener(updateListenerStub)
		device.init()

		return new DeviceElements(device, outputPin, inputPin)
	}

	class DeviceElements {
		SomePeriodicSerialInputDevice device
    SimulatedGpioPinDigitalOutput outputPin
    SimulatedGpioPinDigitalInput inputPin

		DeviceElements(SomePeriodicSerialInputDevice device, SimulatedGpioPinDigitalOutput outputPin, SimulatedGpioPinDigitalInput inputPin) {
			this.device = device
			this.outputPin = outputPin
			this.inputPin = inputPin
		}
	}
}


class SomePeriodicSerialInputDevice extends PeriodicSerialInputDevice {

	String message

	SomePeriodicSerialInputDevice(String id, DeviceType type, SimulatedGpioPinDigitalOutput toDevicePin, SimulatedGpioPinDigitalInput fromDevicePin,
								  SerialConnection serialConnection, Duration periodBetweenAskingForData, Duration acceptablePingPeriod,
								  TimersScheduler scheduler) {
		super(id, type, toDevicePin, fromDevicePin, serialConnection, periodBetweenAskingForData, acceptablePingPeriod, scheduler)
	}

	@Override
	protected void messageReceived(String message) {
		println "Message received in test serial input device"
		this.message = message
	}

	void askForSerialDataNow() {
		askForSerialDataIfDeviceIsAvailable()
	}
}