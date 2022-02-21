package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.configuration.GatewaySystemProperties.ComponentsConfiguration
import com.mqgateway.configuration.GatewaySystemProperties.ComponentsConfiguration.Mcp23017Configuration
import com.mqgateway.configuration.GatewaySystemProperties.ExpanderConfiguration
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.WireColor
import spock.lang.Specification
import spock.lang.Subject

class ConfigValidatorTest extends Specification {

  def validators = [
    new UniqueDeviceIdsValidator(),
    new WireUsageValidator(),
    new ShutterAdditionalConfigValidator(),
    new GateAdditionalConfigValidator(),
    new ReferenceDeviceValidator()
  ]

  GatewaySystemProperties systemProperties = prepareSystemProperties()

  @Subject
  ConfigValidator configValidator = new ConfigValidator(new ObjectMapper(), systemProperties, validators)
  int nextPortNumber = 1

  def "should validation failed when there are more than one device with the same id"() {
    given:
    def gateway = gatewayWith(
      someDevice("1", "duplicate 1_A", DeviceType.RELAY, [WireColor.BLUE]),
      someDevice("2", "duplicate 2_A", DeviceType.RELAY, [WireColor.BLUE_WHITE]),
      someDevice("1", "duplicate 1_B", DeviceType.RELAY, [WireColor.BLUE]),
      someDevice("2", "duplicate 2_B", DeviceType.RELAY, [WireColor.BLUE_WHITE])
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

  def "should validation fail when the same wire is used in many devices"() {
    given:
    def gateway = gatewayWith(
      someDevice("dev-on-blue-1", "dev-on-blue-1", DeviceType.RELAY, [WireColor.BLUE]),
      someDevice("dev-on-green", "dev-on-green", DeviceType.MOTION_DETECTOR, [WireColor.GREEN]),
      someDevice("dev-on-blue-2", "dev-on-blue-2", DeviceType.MOTION_DETECTOR, [WireColor.BLUE])
    )

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded

    def reason = result.failureReasons[0] as WireUsageValidator.SameWireUsedInManyDevices
    reason.devices*.id.toSet() == ["dev-on-blue-1", "dev-on-blue-2"].toSet()
  }

  def "should fail validation of shutter device when any internal device is not of RELAY type"() {
    given:
    def devices = [
      someDevice("withWrongInternalDevice", "shutter1", DeviceType.SHUTTER, [],
                 [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"],
                 [
                   stopRelay  : someDevice("1", "relay1", DeviceType.RELAY, [WireColor.BLUE]),
                   upDownRelay: someDevice("1", "emulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
                 ]),
    ]
    def gateway = gatewayWith(*devices)

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every { it == ShutterAdditionalConfigValidator.NonRelayShutterInternalDevice }

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
                   stopButton : someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
                   closeButton: someDevice("1", "closeRelay", DeviceType.RELAY, [WireColor.BLUE]),
                   openButton : someDevice("1", "openEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE]),
                 ]),
    ]
    def gateway = gatewayWith(*devices)

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every { it == GateAdditionalConfigValidator.UnexpectedGateInternalDevice }

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
                   stopButton      : someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
                   closeButton     : someDevice("1", "closeEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE]),
                   openButton      : someDevice("1", "openEmulatedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE]),
                   closedReedSwitch: someDevice("1", "emulatedSwitchInsteadOfReedSwitch", DeviceType.EMULATED_SWITCH, [WireColor.GREEN]),
                 ]),
    ]
    def gateway = gatewayWith(*devices)

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every { it == GateAdditionalConfigValidator.UnexpectedGateInternalDevice }

    List<GateAdditionalConfigValidator.UnexpectedGateInternalDevice> reasons =
      result.failureReasons.findAll { it instanceof GateAdditionalConfigValidator.UnexpectedGateInternalDevice }
    reasons*.device.id == ["withWrongInternalDevice"]
  }

  def "should fail validation when REFERENCE device references non-existing device"() {
    given:
    DeviceConfiguration referencingDeviceConfig = new DeviceConfiguration("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "non-existing-id")
    GatewayConfiguration gateway = gatewayWith(referencingDeviceConfig)

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    result.failureReasons*.class.every { it == ReferenceDeviceValidator.IncorrectReferencedDevice }
    List<ReferenceDeviceValidator.IncorrectReferencedDevice> reasons = result.failureReasons
    reasons*.referencingDevice.id == ["referencing_device_id"]
    reasons*.referencedDeviceId == ["non-existing-id"]
  }

  static GatewayConfiguration gatewayWith(DeviceConfiguration[] devices) {
    new GatewayConfiguration("1.0", "some gateway", devices.toList())
  }

  def nextPortNumber() { nextPortNumber++ }

  static DeviceConfiguration someDevice(String id = UUID.randomUUID().toString(),
                                        String name = UUID.randomUUID().toString().replace("-", ""),
                                        DeviceType type = DeviceType.RELAY,
                                        List<WireColor> wires = [WireColor.BLUE],
                                        Map<String, String> config = [:],
                                        Map<String, DeviceConfiguration> internalDevices = [:]) {

    new DeviceConfiguration(id, name, type, config, internalDevices)
  }

  static GatewaySystemProperties prepareSystemProperties(ExpanderConfiguration expanderConfiguration = null,
                                                         Mcp23017Configuration mcp23017Configuration = null) {

    def defaultExpanderConfiguration = new ExpanderConfiguration(false)
    def defaultMcp23017Configuration = new Mcp23017Configuration(expanderConfiguration ?: defaultExpanderConfiguration, null)

    def componentsConfiguration = new ComponentsConfiguration(mcp23017Configuration ?: defaultMcp23017Configuration)
    return new GatewaySystemProperties("eth0",
                                       GatewaySystemProperties.SystemPlatform.SIMULATED,
                                       expanderConfiguration ?: defaultExpanderConfiguration,
                                       componentsConfiguration, "localhost")
  }
}
