package com.mqgateway.core.device.serial

import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.utils.ExternalSerialDeviceSimulator
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.utils.SerialStub
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.GpioProvider
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.SimulatedGpioProvider
import com.pi4j.io.gpio.impl.GpioControllerImpl
import com.pi4j.io.gpio.impl.GpioPinImpl
import com.pi4j.io.gpio.impl.PinImpl
import java.time.Duration
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class BME280PeriodicSerialInputDeviceTest extends Specification {

	GpioProvider gpioProvider = new SimulatedGpioProvider()
	def pin1Impl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_INPUT))
	def pin2Impl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_OUTPUT))
	def inputPin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pin1Impl)
	def outputPin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pin2Impl)

	TimersScheduler scheduler = new TimersScheduler()
	SerialStub serialStub = new SerialStub()
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
