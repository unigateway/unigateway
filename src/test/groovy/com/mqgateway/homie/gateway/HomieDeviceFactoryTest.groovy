package com.mqgateway.homie.gateway

import static com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.LAST_PING
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import static com.mqgateway.homie.HomieProperty.DataType.ENUM
import static com.mqgateway.homie.HomieProperty.DataType.FLOAT
import static com.mqgateway.homie.HomieProperty.DataType.INTEGER
import static com.mqgateway.homie.HomieProperty.DataType.STRING
import static com.mqgateway.homie.HomieProperty.Unit.CELSIUS
import static com.mqgateway.homie.HomieProperty.Unit.NONE
import static com.mqgateway.homie.HomieProperty.Unit.PASCAL
import static com.mqgateway.homie.HomieProperty.Unit.PERCENT
import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.Point
import com.mqgateway.core.gatewayconfig.Room
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.utils.MqttClientFactoryStub
import spock.lang.Specification
import spock.lang.Subject

class HomieDeviceFactoryTest extends Specification {

	MqttClientFactoryStub mqttClientFactoryStub = new MqttClientFactoryStub()

	@Subject
	HomieDeviceFactory homieDeviceFactory = new HomieDeviceFactory(mqttClientFactoryStub, "test-version")

	def "should create HomieDevice with nodes and properties based on gateway configuration"() {
		given:
		Gateway gateway = new Gateway("1.0", "gtwName", "127.0.0.1", [
			new Room("room1", [
				new Point("point1", 1, [
					new DeviceConfig("device1", "device1 name", DeviceType.RELAY, [WireColor.BLUE], [:])
				])
			])
		])

		when:
		def homieDevice = homieDeviceFactory.toHomieDevice(gateway, "ethXXX")

		then:
		homieDevice.id == "gtwName"
		homieDevice.name == "gtwName"
		homieDevice.nodes == [
			device1: new HomieNode("gtwName", "device1", "device1 name", "relay",
													   [state: new HomieProperty("gtwName", "device1", "state", "state", ENUM, "ON,OFF", true, true, NONE)])
		]
	}

	def "should create HomieProperties for Relay"() {
		given:
		DeviceConfig device = new DeviceConfig("relay_in_test", "Test Relay", DeviceType.RELAY, [WireColor.BLUE], null)
		Gateway gateway = gateway([room([point([device])])])

		when:
		def homieDevice = homieDeviceFactory.toHomieDevice(gateway, "ethXXX")

		then:
		def node = homieDevice.nodes["relay_in_test"]
		node.properties.keySet() == [STATE.toString()].toSet()
		node.properties[STATE.toString()] == new HomieProperty("gtwName", "relay_in_test", "state", "state", ENUM, "ON,OFF", true, true, NONE)
	}

	def "should create HomieProperties for MotionDetector"() {
		given:
		DeviceConfig device = new DeviceConfig("motiondetector_in_test", "Motion Detector", DeviceType.MOTION_DETECTOR, [WireColor.BLUE], null)
		Gateway gateway = gateway([room([point([device])])])

		when:
		def homieDevice = homieDeviceFactory.toHomieDevice(gateway, "ethXXX")

		then:
		def node = homieDevice.nodes["motiondetector_in_test"]
		node.properties.keySet() == [STATE.toString()].toSet()
		node.properties[STATE.toString()] == new HomieProperty("gtwName", "motiondetector_in_test", "state", "state", ENUM, "ON,OFF", false, false, NONE)
	}

	def "should create HomieProperties for SwitchButton"() {
		given:
		DeviceConfig device = new DeviceConfig("switchButton_in_test", "Switch Button", DeviceType.SWITCH_BUTTON, [WireColor.BLUE_WHITE], null)
		Gateway gateway = gateway([room([point([device])])])

		when:
		def homieDevice = homieDeviceFactory.toHomieDevice(gateway, "ethXXX")

		then:
		def node = homieDevice.nodes["switchButton_in_test"]
		node.properties.keySet() == [STATE.toString()].toSet()
		node.properties[STATE.toString()] == new HomieProperty("gtwName", "switchButton_in_test", "state", "state", ENUM, "PRESSED,RELEASED", false, false, NONE)
	}

	def "should create HomieProperties for ReedSwitch"() {
		given:
		DeviceConfig device = new DeviceConfig("reedSwitch_in_test", "Reed Switch", DeviceType.REED_SWITCH, [WireColor.GREEN], null)
		Gateway gateway = gateway([room([point([device])])])

		when:
		def homieDevice = homieDeviceFactory.toHomieDevice(gateway, "ethXXX")

		then:
		def node = homieDevice.nodes["reedSwitch_in_test"]
		node.properties.keySet() == [STATE.toString()].toSet()
		node.properties[STATE.toString()] == new HomieProperty("gtwName", "reedSwitch_in_test", "state", "state", ENUM, "OPEN,CLOSED", false, true, NONE)
	}

	def "should create HomieProperties for BME280"() {
		given:
		DeviceConfig device = new DeviceConfig("bme280_in_test", "Test BME280", DeviceType.BME280, [WireColor.BLUE], null)
		Gateway gateway = gateway([room([point([device])])])

		when:
		def homieDevice = homieDeviceFactory.toHomieDevice(gateway, "ethXXX")

		then:
		def node = homieDevice.nodes["bme280_in_test"]
		node.properties.keySet() == [TEMPERATURE.toString(), HUMIDITY.toString(), PRESSURE.toString(), UPTIME.toString(), STATE.toString(), LAST_PING.toString()].toSet()
		node.properties[TEMPERATURE.toString()] == new HomieProperty("gtwName", "bme280_in_test", "temperature", "temperature", FLOAT, null, false, true, CELSIUS)
		node.properties[HUMIDITY.toString()] == new HomieProperty("gtwName", "bme280_in_test", "humidity", "humidity", FLOAT, "0:100", false, true, PERCENT)
		node.properties[PRESSURE.toString()] == new HomieProperty("gtwName", "bme280_in_test", "pressure", "pressure", INTEGER, null, false, true, PASCAL)
		node.properties[UPTIME.toString()] == new HomieProperty("gtwName", "bme280_in_test", "uptime", "uptime", INTEGER, null, false, false, NONE)
		node.properties[STATE.toString()] == new HomieProperty("gtwName", "bme280_in_test", "state", "state", ENUM, "STARTING,READY,LOST", false, true, NONE)
		node.properties[LAST_PING.toString()] == new HomieProperty("gtwName", "bme280_in_test", "last_ping", "last_ping", STRING, null, false, true, NONE)
	}
}


