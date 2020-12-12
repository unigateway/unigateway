package com.mqgateway.core.device.serial

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

class BME280PeriodicSerialInputDeviceTest extends Specification {

	def inputPin = new SimulatedGpioPinDigitalInput(PinPullResistance.PULL_UP)
	def outputPin = new SimulatedGpioPinDigitalOutput(PinState.HIGH)

	TimersScheduler scheduler = new TimersScheduler()
  SimulatedSerial serialStub = new SimulatedSerial()
	SerialConnection serialConnection = new SerialConnection(serialStub, 5000L)
	UpdateListenerStub updateListenerStub = new UpdateListenerStub()

	@Subject
	BME280PeriodicSerialInputDevice bme280 = new BME280PeriodicSerialInputDevice("bmeDevice1", outputPin, inputPin, serialConnection,
																				 Duration.ofHours(1), Duration.ofSeconds(30), scheduler)

	PollingConditions conditions = new PollingConditions(timeout: 5)

	void setup() {
		serialConnection.init()
	}

	def "should notify about new properties status when new message is received through serial"() {
		given:
		bme280.addListener(updateListenerStub)
		bme280.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		fakeSerialDevice.setMessageToSend("2073600;2296;54555;99241")
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping" }
			update.newValue != null
		}

		when:
		bme280.askForSerialDataIfDeviceIsAvailable()
		serialConnection.getDataForAllListeners()

		then:
		def uptime = updateListenerStub.receivedUpdates.find {it.propertyId == "uptime"}.newValue
		def temperature = updateListenerStub.receivedUpdates.find {it.propertyId == "temperature"}.newValue
		def humidity = updateListenerStub.receivedUpdates.find {it.propertyId == "humidity"}.newValue
		def pressure = updateListenerStub.receivedUpdates.find {it.propertyId == "pressure"}.newValue

		uptime == "2073600"
		temperature == "22.96"
		humidity == "54.555"
		pressure == "99241"
	}

	def "should not throw exception when cannot parse message"() {
		given:
		bme280.addListener(updateListenerStub)
		bme280.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		fakeSerialDevice.setMessageToSend("2073600;2296;54555;wrongmessage")
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.find {it.propertyId == "last_ping" }
			update.newValue != null
		}

		when:
		bme280.askForSerialDataIfDeviceIsAvailable()

		then:
		notThrown()
	}
}
