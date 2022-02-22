package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.WireColor
import spock.lang.Specification

class YamlParserTest extends Specification {

	YamlParser parser

	void setup() {
		def objectMapper = new ObjectMapper(new YAMLFactory())
		objectMapper.registerModule(new KotlinModule())
		parser = new YamlParser(objectMapper)
	}

	def "should parse Gateway configuration from YAML file"() {
		given:
		def yamlFileBytes = YamlParserTest.getResourceAsStream("/example.gateway.yaml").bytes

		when:
		GatewayConfiguration gateway = parser.parse(parser.toJsonNode(yamlFileBytes))

		then:
		gateway.configVersion == "1.1"
		gateway.name == "TestGw1"
		gateway.mqttHostname == "127.0.0.1"
		gateway.rooms*.name.toSet() == ["workshop", "bedroom", "garage"].toSet()

		def workshop = gateway.rooms.find { it.name == "workshop" }
		workshop.points*.name.toSet()  == ["workshop light switch box"].toSet()

		def workshopPoint1 = workshop.points.find { it.name == "workshop light switch box" }
		workshopPoint1.portNumber == 1
		workshopPoint1.devices*.name.toSet() == ["workshop light", "workshop light switch"].toSet()

		def workshopLightDevice = workshopPoint1.devices.find { it.name == "workshop light" }
		workshopLightDevice.id == "workshop_light"
		workshopLightDevice.type == DeviceType.RELAY
		workshopLightDevice.wires == [WireColor.BLUE_WHITE]

		def workshopLightSwitchDevice = workshopPoint1.devices.find { it.name == "workshop light switch" }
		workshopLightSwitchDevice.id == "workshop_light_switch"
		workshopLightSwitchDevice.type == DeviceType.SWITCH_BUTTON
		workshopLightSwitchDevice.wires == [WireColor.BLUE]
		workshopLightSwitchDevice.config.get("debounceMs") == "50"
	}

	def "should parse shutter device from YAML Gateway configuration file"() {
		given:
		def yamlFileBytes = YamlParserTest.getResourceAsStream("/example.gateway.yaml").bytes

		when:
		GatewayConfiguration gateway = parser.parse(parser.toJsonNode(yamlFileBytes))

		then:
		def bedroom = gateway.rooms.find { it.name == "bedroom" }
		def bedroomPoint = bedroom.points.find { it.name == "bedroom shutters box" }
		def bedroomShutterDevice = bedroomPoint.devices.find {it.name == "bedroom shutter"}
		bedroomShutterDevice.id == "bedroom_shutter"
		bedroomShutterDevice.type == DeviceType.SHUTTER
		bedroomShutterDevice.wires == []

		def stopRelayDevice = bedroomShutterDevice.internalDevices.get("stopRelay")
		stopRelayDevice.name == "bedroom shutter stop relay"
		stopRelayDevice.id == "bedroom_shutter_stop_relay"
		stopRelayDevice.type == DeviceType.RELAY
		stopRelayDevice.wires == [WireColor.BLUE]

		def upDownRelayDevice = bedroomShutterDevice.internalDevices.get("upDownRelay")
		upDownRelayDevice.name == "bedroom shutter up-down relay"
		upDownRelayDevice.id == "bedroom_shutter_updown_relay"
		upDownRelayDevice.type == DeviceType.RELAY
		upDownRelayDevice.wires == [WireColor.BLUE_WHITE]
	}

	def "should parse gate device with REFERENCE device from YAML Gateway configuration file"() {
		given:
		def yamlFileBytes = YamlParserTest.getResourceAsStream("/example.gateway.yaml").bytes

		when:
		GatewayConfiguration gateway = parser.parse(parser.toJsonNode(yamlFileBytes))

		then:
		def garage = gateway.rooms.find { it.name == "garage" }
		def garagePoint = garage.points.find { it.name == "Garage doors relay modules" }
		def garageGateDevice = garagePoint.devices.find {it.name == "Right garage door"}
		garageGateDevice.id == "right_garage_door"
		garageGateDevice.type == DeviceType.GATE
		garageGateDevice.wires == []

		def actionButtonDevice = garageGateDevice.internalDevices.get("actionButton")
		actionButtonDevice.name == "Right garage door action button"
		actionButtonDevice.id == "right_garage_door_action_button"
		actionButtonDevice.type == DeviceType.EMULATED_SWITCH
		actionButtonDevice.wires == [WireColor.BLUE_WHITE]

		def closedReedSwitchDevice = garageGateDevice.internalDevices.get("closedReedSwitch")
		closedReedSwitchDevice.name == "Right garage door closed reed switch reference"
		closedReedSwitchDevice.id == "right_garage_door_closed_ref"
		closedReedSwitchDevice.type == DeviceType.REFERENCE
    closedReedSwitchDevice.referencedDeviceId == "right_garage_door_closed_reed"
	}
}
