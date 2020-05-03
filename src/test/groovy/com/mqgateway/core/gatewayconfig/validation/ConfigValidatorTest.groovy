package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.core.gatewayconfig.ComponentsConfiguration
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.Mcp23017Configuration
import com.mqgateway.core.gatewayconfig.Point
import com.mqgateway.core.gatewayconfig.Room
import com.mqgateway.core.gatewayconfig.SystemConfiguration
import com.mqgateway.core.gatewayconfig.SystemPlatform
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import spock.lang.Specification

class ConfigValidatorTest extends Specification {

	ConfigValidator configValidator = new ConfigValidator(new ObjectMapper())
	int nextPortNumber = 1

	def "should validation failed when there are more than one device with the same id"() {
		given:
		def gateway = gatewayWith(
				roomWith(
					pointWith(
						someDevice("1", "duplicate 1_A", DeviceType.RELAY, [WireColor.BLUE]),
						someDevice("2", "duplicate 2_A", DeviceType.RELAY, [WireColor.BLUE_WHITE])
					)
				),
				roomWith(
					pointWith(
						someDevice("1", "duplicate 1_B", DeviceType.RELAY, [WireColor.BLUE]),
						someDevice("2", "duplicate 2_B", DeviceType.RELAY, [WireColor.BLUE_WHITE])
					)
				)
		)

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		def reason1 = result.failureReasons[0] as UniqueDeviceIdsValidator.DuplicatedDeviceIds
		reason1.duplicates*.name.toSet() == ["duplicate 1_A", "duplicate 1_B"].toSet()
		def reason2 = result.failureReasons[1] as UniqueDeviceIdsValidator.DuplicatedDeviceIds
		reason2.duplicates*.name.toSet() == ["duplicate 2_A", "duplicate 2_B"].toSet()
	}

	def "should validation failed when there are two points with the same port"() {
		given:
		def gateway = gatewayWith(
			roomWith(
				pointWith([someDevice()].toArray() as DeviceConfig[], 1, "A")
			),
			roomWith(
				pointWith([someDevice()].toArray() as DeviceConfig[], 1, "B"),
				pointWith([someDevice()].toArray() as DeviceConfig[], 2, "C")
			)
		)

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded

		def reason = result.failureReasons[0] as UniquePortNumbersForPointsValidator.DuplicatedPortNumbersOnPoints
		reason.points*.name.toSet() == ["A", "B"].toSet()
	}

	def "should validation failed when any device name is more than 32 characters long"() {
		given:
		def devices = [
			new DeviceConfig("1", "12345678901234567890123456789012", DeviceType.RELAY, [WireColor.BLUE_WHITE], [:]),
			new DeviceConfig("2", "123456789012345678901234567890123", DeviceType.RELAY, [WireColor.BLUE], [:])
		]
		def gateway = gatewayWith(roomWith(pointWith(*devices)))

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		result.failureReasons*.class.every {  it == DeviceNameValidator.IllegalDeviceNameValue.class }

		List<DeviceNameValidator.IllegalDeviceNameValue> reasons = result.failureReasons.findAll { it instanceof DeviceNameValidator.IllegalDeviceNameValue }
		reasons*.device.id == ["2"]
	}

	def "should validation fail when the same wire is used in many devices"() {
		given:
		def gateway = gatewayWith(
			roomWith(
				pointWith(
					[
						someDevice("dev-on-blue-1", "dev-on-blue-1", DeviceType.RELAY, [WireColor.BLUE]),
						someDevice("dev-on-green",  "dev-on-green",  DeviceType.MOTION_DETECTOR, [WireColor.GREEN]),
						someDevice("dev-on-blue-2", "dev-on-blue-2", DeviceType.MOTION_DETECTOR, [WireColor.BLUE])
				  	].toArray() as DeviceConfig[],
					1, "B"
				)
			)
		)

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded

		def reason = result.failureReasons[0] as WireUsageValidator.SameWireUsedInManyDevices
		reason.devices*.id.toSet() == ["dev-on-blue-1", "dev-on-blue-2"].toSet()
	}

	def "should accept two 1-wire devices on the same wire"() {
		given:
		def gateway = gatewayWith(
			roomWith(
				pointWith(
					someDevice("onewire-on-brown-1", "onewire-on-brown-1", DeviceType.DS18B20, [WireColor.BROWN], [oneWireAddress: "28-00da18b10001"]),
					someDevice("dev-on-green",  "dev-on-green",  DeviceType.MOTION_DETECTOR, [WireColor.GREEN]),
					someDevice("onewire-on-brown-2", "onewire-on-brown-2", DeviceType.DS18B20, [WireColor.BROWN], [oneWireAddress: "28-0115a3a147ff"])
				)
			)
		)

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		result.succeeded
	}

	def "should validation fail when one wire device misses oneWireAddress in config"() {
		given:
		def gateway = gatewayWith(
			roomWith(
				pointWith(
					someDevice("one-wire-without-address-1", "one-wire-without-address-1", DeviceType.DS18B20, [WireColor.BROWN])
				),
				pointWith(
					someDevice("one-wire-with-address-2", "one-wire-with-address-2", DeviceType.DS18B20, [WireColor.BROWN], [oneWireAddress: "28-00da18b10001"]),
					someDevice("one-wire-without-address-3", "one-wire-without-address-3", DeviceType.DS18B20, [WireColor.BROWN_WHITE])
				)
			)
		)

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded

		result.failureReasons*.class.every {  it == OneWireDeviceAddressValidator.OneWireDeviceAddressMissing.class }

		result.failureReasons*.device*.id.toSet() == ["one-wire-without-address-1", "one-wire-without-address-3"].toSet()
	}

	SystemConfiguration someSystemConfiguration() {
		new SystemConfiguration("eth0", SystemPlatform.NANOPI, new ComponentsConfiguration(new Mcp23017Configuration(["20", "21"])))
	}

	Gateway gatewayWith(SystemConfiguration systemConfiguration = someSystemConfiguration(), Room[] rooms) {
		new Gateway("1.0", "some gateway", "192.168.1.123", systemConfiguration, rooms.toList())
	}

	Room roomWith(Point[] points, String name = UUID.randomUUID().toString()) {
		new Room(name, points.toList())
	}

	Point pointWith(DeviceConfig[] devices, int portNumber = nextPortNumber(), String name = UUID.randomUUID().toString()) {
		new Point(name, portNumber, devices.toList())
	}

	def nextPortNumber()  { nextPortNumber++ }

	DeviceConfig someDevice(String id = UUID.randomUUID().toString(),
							String name = UUID.randomUUID().toString().replace("-", ""),
							DeviceType type = DeviceType.RELAY,
							List<WireColor> wires = [WireColor.BLUE],
							Map<String, String> config = [:]) {

		new DeviceConfig(id, name, type, wires, config)
	}
}
