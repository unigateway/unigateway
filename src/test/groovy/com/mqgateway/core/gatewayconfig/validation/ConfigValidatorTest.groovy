package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.configuration.GatewaySystemProperties.ComponentsConfiguration
import com.mqgateway.configuration.GatewaySystemProperties.ComponentsConfiguration.Mcp23017Configuration
import com.mqgateway.configuration.GatewaySystemProperties.ComponentsConfiguration.MySensors
import com.mqgateway.configuration.GatewaySystemProperties.ExpanderConfiguration
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.Point
import com.mqgateway.core.gatewayconfig.Room
import com.mqgateway.core.gatewayconfig.WireColor
import spock.lang.Specification
import spock.lang.Subject

class ConfigValidatorTest extends Specification {

  def validators = [
    new UniqueDeviceIdsValidator(),
    new UniquePortNumbersForPointsValidator(),
    new WireUsageValidator(),
    new MySensorsDeviceWiresValidator(),
    new MySensorsDeviceAdditionalConfigValidator(),
    new ShutterAdditionalConfigValidator(),
    new GateAdditionalConfigValidator(),
    new PortNumbersRangeValidator(),
    new ReferenceDeviceValidator()
  ]

  GatewaySystemProperties systemProperties = prepareSystemProperties()

	@Subject
	ConfigValidator configValidator = new ConfigValidator(new ObjectMapper(), systemProperties, validators)
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

	def "should fail validation of MySensors device when wires are not exactly BROWN and BROWN_WHITE"() {
		given:
		def devices = [
			someDevice("withWrongConfig1", "device1", DeviceType.BME280, [], [mySensorsNodeId: "3"]),
			someDevice("withWrongConfig2", "device2", DeviceType.BME280, [WireColor.GREEN], [mySensorsNodeId: "4"]),
			someDevice("withWrongConfig3", "device3", DeviceType.BME280, [WireColor.BROWN, WireColor.BLUE], [mySensorsNodeId: "5"]),
			someDevice("withWrongConfig4", "device4", DeviceType.BME280, [WireColor.BLUE, WireColor.BLUE_WHITE, WireColor.GREEN_WHITE], [mySensorsNodeId: "6"])
		]
		def gateway = gatewayWith(roomWith(pointWith(*devices)))

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		result.failureReasons*.class.every {  it == MySensorsDeviceWiresValidator.WrongWiresConfigurationForMySensorsDevice }

		List<MySensorsDeviceWiresValidator.WrongWiresConfigurationForMySensorsDevice> reasons =
			result.failureReasons.findAll { it instanceof MySensorsDeviceWiresValidator.WrongWiresConfigurationForMySensorsDevice }
		reasons*.device.id.toSet() == ["withWrongConfig1", "withWrongConfig2", "withWrongConfig3", "withWrongConfig4"].toSet()
	}

	def "should fail validation of MySensors-only device when mySensorsNodeId configuration is missing"() {
		given:
		def devices = [
			someDevice("withWrongConfig", "device1", DeviceType.BME280, [WireColor.BROWN, WireColor.BROWN_WHITE])
		]
		def gateway = gatewayWith(roomWith(pointWith(*devices)))

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		result.failureReasons*.class.every {  it == MySensorsDeviceAdditionalConfigValidator.MissingNodeId }

		List<MySensorsDeviceAdditionalConfigValidator.MissingNodeId> reasons =
			result.failureReasons.findAll { it instanceof MySensorsDeviceAdditionalConfigValidator.MissingNodeId }
		reasons*.device.id == ["withWrongConfig"]
	}

	def "should fail validation of shutter device when any internal device is not of RELAY type"() {
		given:
		def devices = [
			someDevice("withWrongInternalDevice", "shutter1", DeviceType.SHUTTER, [],
					   [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"],
					   [
						   stopRelay: someDevice("1", "relay1", DeviceType.RELAY, [WireColor.BLUE]),
						   upDownRelay: someDevice("1", "emulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
					   ]),
		]
		def gateway = gatewayWith(roomWith(pointWith(*devices)))

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		result.failureReasons*.class.every {  it == ShutterAdditionalConfigValidator.NonRelayShutterInternalDevice }

		List<ShutterAdditionalConfigValidator.NonRelayShutterInternalDevice> reasons =
			result.failureReasons.findAll { it instanceof ShutterAdditionalConfigValidator.NonRelayShutterInternalDevice }
		reasons*.device.id == ["withWrongInternalDevice"]
	}

	def "should fail validation of gate device when button internal device is not of type EMULATED_BUTTON"() {
		given:
		def devices = [
			someDevice("withWrongInternalDevice", "gate1", DeviceType.GATE, [],
					   [:],
					   [
						   stopButton: someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
						   closeButton: someDevice("1", "closeRelay", DeviceType.RELAY, [WireColor.BLUE]),
						   openButton: someDevice("1", "openEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE]),
					   ]),
		]
		def gateway = gatewayWith(roomWith(pointWith(*devices)))

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		result.failureReasons*.class.every {  it == GateAdditionalConfigValidator.UnexpectedGateInternalDevice }

		List<GateAdditionalConfigValidator.UnexpectedGateInternalDevice> reasons =
			result.failureReasons.findAll { it instanceof GateAdditionalConfigValidator.UnexpectedGateInternalDevice }
		reasons*.device.id == ["withWrongInternalDevice"]
	}

	def "should fail validation of gate device when reed switch internal device is not of type REED_SWITCH"() {
		given:
		def devices = [
			someDevice("withWrongInternalDevice", "gate1", DeviceType.GATE, [],
					   [:],
					   [
						   stopButton: someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
						   closeButton: someDevice("1", "closeEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
						   openButton: someDevice("1", "openEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE]),
               closedReedSwitch: someDevice("1", "emulatedSwitchInsteadOfReedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.GREEN]),
					   ]),
		]
		def gateway = gatewayWith(roomWith(pointWith(*devices)))

		when:
		def result = configValidator.validateGateway(gateway)

		then:
		!result.succeeded
		result.failureReasons*.class.every {  it == GateAdditionalConfigValidator.UnexpectedGateInternalDevice }

		List<GateAdditionalConfigValidator.UnexpectedGateInternalDevice> reasons =
			result.failureReasons.findAll { it instanceof GateAdditionalConfigValidator.UnexpectedGateInternalDevice }
		reasons*.device.id == ["withWrongInternalDevice"]
	}

  def "should fail validation of device when using port number higher than 16 and I/O expander is disabled"() {
    given:
    GatewaySystemProperties systemProperties = prepareSystemProperties(new ExpanderConfiguration(false))
    ConfigValidator configValidator = new ConfigValidator(new ObjectMapper(), systemProperties, validators)
    def gateway = gatewayWith(
      roomWith(
        pointWith([someDevice()].toArray() as DeviceConfig[], 17, "Point with too high port number 1"),
        pointWith([someDevice()].toArray() as DeviceConfig[], 16, "Point with proper port number"),
        pointWith([someDevice()].toArray() as DeviceConfig[], 18, "Point with too high port number 2")
      )
    )

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every {  it == PortNumbersRangeValidator.PortNumberOutOfRange }

    List<PortNumbersRangeValidator.PortNumberOutOfRange> reasons =
      result.failureReasons.findAll { it instanceof PortNumbersRangeValidator.PortNumberOutOfRange }
    reasons*.point.name.toSet() == ["Point with too high port number 1", "Point with too high port number 2"].toSet()
  }

  def "should fail validation of device when using port number higher than 32 and I/O expander is enabled"() {
    given:
    GatewaySystemProperties systemProperties = prepareSystemProperties(new ExpanderConfiguration(true))
    ConfigValidator configValidator = new ConfigValidator(new ObjectMapper(), systemProperties, validators)
    def gateway = gatewayWith(
      roomWith(
        pointWith([someDevice()].toArray() as DeviceConfig[], 16, "Point with proper port number 1"),
        pointWith([someDevice()].toArray() as DeviceConfig[], 32, "Point with proper port number 2"),
        pointWith([someDevice()].toArray() as DeviceConfig[], 33, "Point with too high port number")
      )
    )

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every {  it == PortNumbersRangeValidator.PortNumberOutOfRange }

    List<PortNumbersRangeValidator.PortNumberOutOfRange> reasons =
      result.failureReasons.findAll { it instanceof PortNumbersRangeValidator.PortNumberOutOfRange }
    reasons*.point.name == ["Point with too high port number"]
  }

  def "should fail validation when REFERENCE device references non-existing device"() {
    given:
    DeviceConfig referencingDeviceConfig = new DeviceConfig("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "non-existing-id")
    Gateway gateway = gatewayWith(roomWith(pointWith(referencingDeviceConfig)))

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every {  it == ReferenceDeviceValidator.IncorrectReferencedDevice }
    List<ReferenceDeviceValidator.IncorrectReferencedDevice> reasons = result.failureReasons
    reasons*.referencingDevice.id == ["referencing_device_id"]
    reasons*.referencedDeviceId == ["non-existing-id"]
  }

	static Gateway gatewayWith(Room[] rooms) {
		new Gateway("1.0", "some gateway", "192.168.1.123", rooms.toList())
	}

	static Room roomWith(Point[] points, String name = UUID.randomUUID().toString()) {
		new Room(name, points.toList())
	}

	Point pointWith(DeviceConfig[] devices, int portNumber = nextPortNumber(), String name = UUID.randomUUID().toString()) {
		new Point(name, portNumber, devices.toList())
	}

	def nextPortNumber()  { nextPortNumber++ }

	static DeviceConfig someDevice(String id = UUID.randomUUID().toString(),
								   String name = UUID.randomUUID().toString().replace("-", ""),
								   DeviceType type = DeviceType.RELAY,
								   List<WireColor> wires = [WireColor.BLUE],
								   Map<String, String> config = [:],
								   Map<String, DeviceConfig> internalDevices = [:]) {

		new DeviceConfig(id, name, type, wires, config, internalDevices)
	}

  static GatewaySystemProperties prepareSystemProperties(ExpanderConfiguration expanderConfiguration = null,
                                                         Mcp23017Configuration mcp23017Configuration = null,
                                                         MySensors mySensors = null) {

    def defaultExpanderConfiguration = new ExpanderConfiguration(false)
    def defaultMcp23017Configuration = new Mcp23017Configuration(expanderConfiguration ?: defaultExpanderConfiguration, null)
    def mySensorsDefaultConfiguration = new MySensors(true, "/dev/myserial")

    def componentsConfiguration = new ComponentsConfiguration(mcp23017Configuration ?: defaultMcp23017Configuration,
                                                              mySensors ?: mySensorsDefaultConfiguration)
    return new GatewaySystemProperties("eth0",
                                       GatewaySystemProperties.SystemPlatform.SIMULATED,
                                       expanderConfiguration ?: defaultExpanderConfiguration,
                                       componentsConfiguration)
  }
}
