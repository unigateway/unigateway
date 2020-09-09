package com.mqgateway.core.device

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.mqgateway.core.device.serial.BME280PeriodicSerialInputDevice
import com.mqgateway.core.device.serial.DHT22PeriodicSerialInputDevice
import com.mqgateway.core.serial.SerialConnection
import com.mqgateway.utils.SerialStub
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.mcpexpander.ExpanderPinProvider
import spock.lang.Specification
import spock.lang.Subject

class DeviceFactoryTest extends Specification {

	ExpanderPinProvider pinProvider = Mock()

	@Subject
	DeviceFactory deviceFactory = new DeviceFactory(pinProvider, new SerialConnection(new SerialStub(), 5000))

	def "should create relay"() {
		given:
		def relayDeviceConfig = new DeviceConfig("myRelay", "Test relay", DeviceType.RELAY, [WireColor.BLUE], null)
		Gateway gateway = gateway([room([point("point name", 2, [relayDeviceConfig])])])
		pinProvider.pinDigitalOutput(2, WireColor.BLUE, "myRelay_pin", PinState.HIGH) >> Mock(GpioPinDigitalOutput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof RelayDevice
		device.id == "myRelay"
		device.type == DeviceType.RELAY
	}

	def "should create switch button"() {
		given:
		def deviceConfig = new DeviceConfig("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON, [WireColor.GREEN], ["debounceMs": "124"])
		Gateway gateway = gateway([room([point("point name", 5, [deviceConfig])])])
		pinProvider.pinDigitalInput(5, WireColor.GREEN, "mySwitchButton_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof SwitchButtonDevice
		device.id == "mySwitchButton"
		device.type == DeviceType.SWITCH_BUTTON
	}

	def "should create reed switch"() {
		given:
		def deviceConfig = new DeviceConfig("myReedSwitch", "Test ReedSwitch", DeviceType.REED_SWITCH, [WireColor.GREEN_WHITE], ["debounceMs": "54"])
		Gateway gateway = gateway([room([point("point name", 4, [deviceConfig])])])
		pinProvider.pinDigitalInput(4, WireColor.GREEN_WHITE, "myReedSwitch_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof ReedSwitchDevice
		device.id == "myReedSwitch"
		device.type == DeviceType.REED_SWITCH
	}

	def "should create motion detector"() {
		given:
		def deviceConfig = new DeviceConfig("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR, [WireColor.BLUE], ["debounceMs": "1"])
		Gateway gateway = gateway([room([point("point name", 12, [deviceConfig])])])
		pinProvider.pinDigitalInput(12, WireColor.BLUE, "myMotionDetector_pin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof MotionSensorDevice
		device.id == "myMotionDetector"
		device.type == DeviceType.MOTION_DETECTOR
	}

	def "should create BME280"() {
		given:
		def deviceConfig = new DeviceConfig("myBME280", "Test BME280 device", DeviceType.BME280, [WireColor.GREEN, WireColor.GREEN_WHITE],
											[periodBetweenAskingForDataInSec: "30", acceptablePingPeriodInSec: "20"])
		Gateway gateway = gateway([room([point("point name", 10, [deviceConfig])])])
		pinProvider.pinDigitalOutput(10, WireColor.GREEN, "myBME280_toDevicePin", PinState.HIGH) >> Mock(GpioPinDigitalOutput)
		pinProvider.pinDigitalInput(10, WireColor.GREEN_WHITE, "myBME280_fromDevicePin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof BME280PeriodicSerialInputDevice
		device.id == "myBME280"
		device.type == DeviceType.BME280
	}

	def "should omit creation of serial device (e.g. bme280) when serial connection is not passed to factory"() {
		given:
		DeviceFactory deviceFactory = new DeviceFactory(pinProvider, null)
		def deviceConfig = new DeviceConfig("myBME280", "Test BME280 device", DeviceType.BME280, [WireColor.GREEN, WireColor.GREEN_WHITE],
											[periodBetweenAskingForDataInSec: "30", acceptablePingPeriodInSec: "20"])
		Gateway gateway = gateway([room([point("point name", 10, [deviceConfig])])])
		pinProvider.pinDigitalOutput(10, WireColor.GREEN, "myBME280_toDevicePin", PinState.LOW) >> Mock(GpioPinDigitalOutput)
		pinProvider.pinDigitalInput(10, WireColor.GREEN_WHITE, "myBME280_fromDevicePin", PinPullResistance.PULL_DOWN) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		devices.isEmpty()
	}

	def "should create DHT22"() {
		given:
		def deviceConfig = new DeviceConfig("myDHT22", "Test DHT22 device", DeviceType.DHT22, [WireColor.GREEN, WireColor.GREEN_WHITE],
											[periodBetweenAskingForDataInSec: "30", acceptablePingPeriodInSec: "20"])
		Gateway gateway = gateway([room([point("point name", 10, [deviceConfig])])])
		pinProvider.pinDigitalOutput(10, WireColor.GREEN, "myDHT22_toDevicePin", PinState.HIGH) >> Mock(GpioPinDigitalOutput)
		pinProvider.pinDigitalInput(10, WireColor.GREEN_WHITE, "myDHT22_fromDevicePin", PinPullResistance.PULL_UP) >> Mock(GpioPinDigitalInput)

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof DHT22PeriodicSerialInputDevice
		device.id == "myDHT22"
		device.type == DeviceType.DHT22
	}
}
