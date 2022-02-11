package com.mqgateway.core.gatewayconfig

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import spock.lang.Specification

class DeviceConfigurationTest extends Specification {

  def "should return itself as referenced device config when it is of type different than REFERENCE"() {
    given:
    DeviceConfiguration deviceConfig = new DeviceConfiguration("test_id", "test name", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
    GatewayConfiguration gateway = gateway([room([point([deviceConfig])])])

    when:
    def dereferencedDeviceConfig = deviceConfig.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig == deviceConfig
  }

  def "should return referenced device config when device config is referencing actual device config directly"() {
    given:
    DeviceConfiguration actualDeviceConfig = new DeviceConfiguration("actual_device_id", "test name", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
    DeviceConfiguration referencingDeviceConfig = new DeviceConfiguration("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "actual_device_id")
    GatewayConfiguration gateway = gateway([room([point([actualDeviceConfig]), point([referencingDeviceConfig])])])

    when:
    def dereferencedDeviceConfig = referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig == actualDeviceConfig
  }

  def "should return referenced device config when reference is going through few levels"() {
    given:
    DeviceConfiguration actualDeviceConfig = new DeviceConfiguration("actual_device_id", "test name", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
    DeviceConfiguration referencingDeviceConfig1 = new DeviceConfiguration("referencing_device_1", "test name", DeviceType.REFERENCE, [], [:], [:], "actual_device_id")
    DeviceConfiguration referencingDeviceConfig2 = new DeviceConfiguration("referencing_device_2", "test name", DeviceType.REFERENCE, [], [:], [:], "referencing_device_1")
    DeviceConfiguration referencingDeviceConfig3 = new DeviceConfiguration("referencing_device_3", "test name", DeviceType.REFERENCE, [], [:], [:], "referencing_device_2")
    GatewayConfiguration gateway = gateway([room([
      point([actualDeviceConfig]),
      point([referencingDeviceConfig1, referencingDeviceConfig2]),
      point([referencingDeviceConfig3])
    ])])

    when:
    def dereferencedDeviceConfig = referencingDeviceConfig3.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig == actualDeviceConfig
  }

  def "should return additional config of actual device when getting referenced device"() {
    given:
    DeviceConfiguration actualDeviceConfig = new DeviceConfiguration("actual_device_id", "test name", DeviceType.RELAY, [WireColor.BLUE], ["something":"actual device value"], [:])
    DeviceConfiguration referencingDeviceConfig = new DeviceConfiguration("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "actual_device_id")
    GatewayConfiguration gateway = gateway([room([point([actualDeviceConfig]), point([referencingDeviceConfig])])])

    when:
    def dereferencedDeviceConfig = referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig.config.something == "actual device value"
  }

  def "should fail with exception when device references to non-existing device"() {
    given:
    DeviceConfiguration referencingDeviceConfig = new DeviceConfiguration("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "non_existing_device")
    GatewayConfiguration gateway = gateway([room([point([referencingDeviceConfig])])])

    when:
    referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    thrown(GatewayConfiguration.UnknownDeviceIdException)
  }

  def "should fail with exception when device is REFERENCE, but it doesn't have deviceReferenceId specified"() {
    given:
    DeviceConfiguration referencingDeviceConfig = new DeviceConfiguration("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], null)
    GatewayConfiguration gateway = gateway([room([point([referencingDeviceConfig])])])

    when:
    referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    thrown(DeviceConfiguration.UnexpectedDeviceConfigurationException)
  }
}
