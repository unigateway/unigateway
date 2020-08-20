package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.serial.ExternalSerialDeviceSimulator
import com.mqgateway.core.serial.SerialConnection
import com.mqgateway.core.serial.SerialStub
import com.mqgateway.utils.UpdateListenerStub
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
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

class PeriodicSerialInputDeviceTest extends Specification {

	GpioProvider gpioProvider = new SimulatedGpioProvider()
	def pin1Impl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_INPUT))
	def pin2Impl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_OUTPUT))
	def inputPin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pin1Impl)
	def outputPin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pin2Impl)

	SerialStub serialStub = new SerialStub()
	SerialConnection serialConnection = new SerialConnection(serialStub, 5000L)
	UpdateListenerStub updateListenerStub = new UpdateListenerStub()

	@Subject
	PeriodicSerialInputDevice device = new SomePeriodicSerialInputDevice("someDevice", DeviceType.BME280, outputPin, inputPin, serialConnection,
																		 Duration.ofHours(1), Duration.ofSeconds(10))

	PollingConditions conditions = new PollingConditions(timeout: 5)

	void setup() {
		serialConnection.init()
	}

	def "should set lastPing property on ping (HIGH state) from device"() {
		given:
		device.addListener(updateListenerStub)
		device.init()
		inputPin.low()

		when:
		inputPin.high()

		then:
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.first()
			update.propertyId == "lastPing"
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
			def update = updateListenerStub.receivedUpdates.first()
			update.propertyId == "lastPing"
			update.newValue != null
		}

		when:
		device.askForSerialDataNow()

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

		then:
		device.message == null
	}

	def "should throw exception if message not received"() {
		given:
		outputPin.low()
		SerialConnection serialConnection = new SerialConnection(serialStub, 50)
		PeriodicSerialInputDevice device = new SomePeriodicSerialInputDevice("someDevice", DeviceType.BME280, outputPin, inputPin, serialConnection,
																			 Duration.ofHours(1), Duration.ofSeconds(10))
		device.addListener(updateListenerStub)
		device.init()
		serialConnection.init()
		def fakeSerialDevice = new ExternalSerialDeviceSimulator(outputPin, inputPin, serialStub)
		fakeSerialDevice.ping()
		conditions.eventually {
			def update = updateListenerStub.receivedUpdates.first()
			update.propertyId == "lastPing"
			update.newValue != null
		}

		when:
		device.askForSerialDataNow()

		then:
		thrown(SerialMessageNotReceivedException)

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
			devicesElements.collect {it.device.id }.every {deviceId ->
				def update = updateListenerStub.receivedUpdates.find {it.deviceId == deviceId && it.propertyId == "lastPing" }
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

		then:
		devicesElements.every {
			it.device.message == "message for ${it.device.id}"
		}
	}

	DeviceElements prepareSerialDevice(String name) {
		def pin1Impl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_INPUT))
		def pin2Impl = new PinImpl("", 0, "", EnumSet<PinMode>.of(PinMode.DIGITAL_OUTPUT))
		def inputPin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pin1Impl)
		def outputPin = new GpioPinImpl(new GpioControllerImpl(gpioProvider), gpioProvider, pin2Impl)

		def device = new SomePeriodicSerialInputDevice(name, DeviceType.BME280, outputPin, inputPin, serialConnection,
														Duration.ofHours(1), Duration.ofSeconds(10))
		device.addListener(updateListenerStub)
		device.init()

		return new DeviceElements(device, outputPin, inputPin)
	}

	class DeviceElements {
		SomePeriodicSerialInputDevice device
		GpioPinImpl outputPin
		GpioPinImpl inputPin

		DeviceElements(SomePeriodicSerialInputDevice device, GpioPinImpl outputPin, GpioPinImpl inputPin) {
			this.device = device
			this.outputPin = outputPin
			this.inputPin = inputPin
		}
	}
}


class SomePeriodicSerialInputDevice extends PeriodicSerialInputDevice {

	String message

	SomePeriodicSerialInputDevice(String id, DeviceType type, GpioPinDigitalOutput toDevicePin, GpioPinDigitalInput fromDevicePin,
								  SerialConnection serialConnection, Duration periodBetweenAskingForData, Duration acceptablePingPeriod) {
		super(id, type, toDevicePin, fromDevicePin, serialConnection, periodBetweenAskingForData, acceptablePingPeriod)
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