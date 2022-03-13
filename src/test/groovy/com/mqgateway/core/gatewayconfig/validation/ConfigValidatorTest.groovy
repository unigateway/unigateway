package com.mqgateway.core.gatewayconfig.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.GatewaySystemProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import spock.lang.Specification
import spock.lang.Subject

class ConfigValidatorTest extends Specification {

  def validators = [
    new UniqueDeviceIdsValidator(),
    new ShutterAdditionalConfigValidator(),
    new GateAdditionalConfigValidator(),
    new ReferenceDeviceValidator()
  ]

  GatewaySystemProperties systemProperties = prepareSystemProperties()

  @Subject
  ConfigValidator configValidator = new ConfigValidator(new ObjectMapper(), systemProperties, validators)

  def "should validation failed when there are more than one device with the same id"() {
    given:
    def gateway = gatewayWith(
      someDevice("1", "duplicate 1_A", DeviceType.RELAY),
      someDevice("2", "duplicate 2_A", DeviceType.RELAY),
      someDevice("1", "duplicate 1_B", DeviceType.RELAY),
      someDevice("2", "duplicate 2_B", DeviceType.RELAY)
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

  def "should fail validation of shutter device when any internal device is not of RELAY type"() {
    given:
    def devices = [
      someDevice("1", "relay1", DeviceType.RELAY),
      someDevice("2", "emulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice(
        "withWrongInternalDevice", "shutter1", DeviceType.SHUTTER, [
        stopRelay  : new InternalDeviceConfiguration("1"),
        upDownRelay: new InternalDeviceConfiguration("2"),
      ],
        [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"]
      ),
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
      someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("2", "closeRelay", DeviceType.RELAY),
      someDevice("3", "openEmulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("withWrongInternalDevice", "gate1", DeviceType.GATE, [
        stopButton : new InternalDeviceConfiguration("1"),
        closeButton: new InternalDeviceConfiguration("2"),
        openButton : new InternalDeviceConfiguration("3")
      ])
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
      someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("2", "closeEmulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("3", "openEmulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("4", "emulatedSwitchInsteadOfReedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("withWrongInternalDevice", "gate1", DeviceType.GATE, [
        stopButton      : new InternalDeviceConfiguration("1"),
        closeButton     : new InternalDeviceConfiguration("2"),
        openButton      : new InternalDeviceConfiguration("3"),
        closedReedSwitch: new InternalDeviceConfiguration("4")
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

  def "should fail validation when device references non-existing device as internal device"() {
    given:
    def devices = [
      someDevice("1", "stopEmulatedSwitch", DeviceType.EMULATED_SWITCH),
      someDevice("2", "closeRelay", DeviceType.RELAY),
      someDevice("referencing_device_id", "test name", DeviceType.GATE, [
        stopButton : new InternalDeviceConfiguration("1"),
        closeButton: new InternalDeviceConfiguration("2"),
        openButton : new InternalDeviceConfiguration("non-existing-id")
      ])
    ]
    GatewayConfiguration gateway = gatewayWith(*devices)

    when:
    def result = configValidator.validateGateway(gateway)

    then:
    !result.succeeded
    ReferenceDeviceValidator.IncorrectReferencedDevice failureReason =
      result.failureReasons.find {it.class == ReferenceDeviceValidator.IncorrectReferencedDevice } as ReferenceDeviceValidator.IncorrectReferencedDevice
    failureReason.referencingDevice.id == "referencing_device_id"
    failureReason.referencedDeviceId == "non-existing-id"
  }

  static GatewayConfiguration gatewayWith(DeviceConfiguration[] devices) {
    new GatewayConfiguration("1.0", "unigateway-id", "some gateway", devices.toList())
  }


  static DeviceConfiguration someDevice(String id = UUID.randomUUID().toString(),
                                        String name = UUID.randomUUID().toString().replace("-", ""),
                                        DeviceType type = DeviceType.RELAY,
                                        Map<String, InternalDeviceConfiguration> internalDevices = [:],
                                        Map<String, String> config = [:]) {

    new DeviceConfiguration(id, name, type, [:], internalDevices, config)
  }

  static GatewaySystemProperties prepareSystemProperties() {

    return new GatewaySystemProperties("eth0", "SIMULATED", [:], "localhost")
  }
}
