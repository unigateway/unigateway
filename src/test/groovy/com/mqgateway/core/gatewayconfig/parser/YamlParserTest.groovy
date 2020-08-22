package com.mqgateway.core.gatewayconfig.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
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
		Gateway gateway = parser.parse(parser.toJsonNode(yamlFileBytes))

		then:
		gateway.configVersion == "1.0"
		gateway.name == "TestGw1"
		gateway.mqttHostname == "127.0.0.1"
		gateway.rooms*.name.toSet() == ["workshop", "bedroom"].toSet()

		def workshop = gateway.rooms.find { it.name == "workshop" }
		workshop.points*.name.toSet()  == ["workshop light switch box", "workshop multi-box"].toSet()

		def workshopPoint1 = workshop.points.find { it.name == "workshop light switch box" }
		workshopPoint1.portNumber == 1
		workshopPoint1.devices*.name.toSet() == ["workshop light", "workshop light switch"].toSet()

		def workshopPoint2 = workshop.points.find { it.name == "workshop multi-box" }
		workshopPoint2.portNumber == 3
		workshopPoint2.devices*.name.toSet() == ["bme280 device"].toSet()

		def workshopLightDevice = workshopPoint1.devices.find { it.name == "workshop light" }
		workshopLightDevice.id == "workshop_light"
		workshopLightDevice.type == DeviceType.RELAY
		workshopLightDevice.wires == [WireColor.BLUE_WHITE]

		def workshopLightSwitchDevice = workshopPoint1.devices.find { it.name == "workshop light switch" }
		workshopLightSwitchDevice.id == "workshop_light_switch"
		workshopLightSwitchDevice.type == DeviceType.SWITCH_BUTTON
		workshopLightSwitchDevice.wires == [WireColor.BLUE]
		workshopLightSwitchDevice.config.get("debounceMs") == "50"

		def workshopBme280Device = workshopPoint2.devices.find { it.name == "bme280 device" }
		workshopBme280Device.id == "bme280_workshop"
		workshopBme280Device.type == DeviceType.BME280
		workshopBme280Device.wires == [WireColor.GREEN, WireColor.GREEN_WHITE]
		workshopBme280Device.config.get("periodBetweenAskingForDataInSec") == "30"
		workshopBme280Device.config.get("acceptablePingPeriodInSec") == "20"
	}
}
