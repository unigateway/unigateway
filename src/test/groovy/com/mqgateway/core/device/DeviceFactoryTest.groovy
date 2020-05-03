package com.mqgateway.core.device

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.mqgateway.core.onewire.OneWireBus
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
	OneWireBus oneWireBus = new OneWireBus(File.createTempDir().absolutePath, 50000)

	@Subject
	DeviceFactory deviceFactory = new DeviceFactory(pinProvider, oneWireBus)

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

	def "should create DS18B20"() {
		given:
		def deviceConfig = new DeviceConfig("myDS18B20", "Test DS18B20", DeviceType.DS18B20, [WireColor.BROWN], ["oneWireAddress": "28-00da18b20000"])
		Gateway gateway = gateway([room([point("point name", 6, [deviceConfig])])])

		when:
		def devices = deviceFactory.createAll(gateway)

		then:
		def device = devices.first()
		device instanceof DS18B20Device
		device.id == "myDS18B20"
		device.type == DeviceType.DS18B20
	}
}
