package com.mqgateway.core.gatewayconfig

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import spock.lang.Specification

class DeviceConfigTest extends Specification {

  def "should return itself as referenced device config when it is of type different than REFERENCE"() {
    given:
    DeviceConfig deviceConfig = new DeviceConfig("test_id", "test name", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
    Gateway gateway = gateway([room([point([deviceConfig])])])

    when:
    def dereferencedDeviceConfig = deviceConfig.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig == deviceConfig
  }

  def "should return referenced device config when device config is referencing actual device config directly"() {
    given:
    DeviceConfig actualDeviceConfig = new DeviceConfig("actual_device_id", "test name", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
    DeviceConfig referencingDeviceConfig = new DeviceConfig("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "actual_device_id")
    Gateway gateway = gateway([room([point([actualDeviceConfig]), point([referencingDeviceConfig])])])

    when:
    def dereferencedDeviceConfig = referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig == actualDeviceConfig
  }

  def "should return referenced device config when reference is going through few levels"() {
    given:
    DeviceConfig actualDeviceConfig = new DeviceConfig("actual_device_id", "test name", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
    DeviceConfig referencingDeviceConfig1 = new DeviceConfig("referencing_device_1", "test name", DeviceType.REFERENCE, [], [:], [:], "actual_device_id")
    DeviceConfig referencingDeviceConfig2 = new DeviceConfig("referencing_device_2", "test name", DeviceType.REFERENCE, [], [:], [:], "referencing_device_1")
    DeviceConfig referencingDeviceConfig3 = new DeviceConfig("referencing_device_3", "test name", DeviceType.REFERENCE, [], [:], [:], "referencing_device_2")
    Gateway gateway = gateway([room([
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
    DeviceConfig actualDeviceConfig = new DeviceConfig("actual_device_id", "test name", DeviceType.RELAY, [WireColor.BLUE], ["something":"actual device value"], [:])
    DeviceConfig referencingDeviceConfig = new DeviceConfig("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "actual_device_id")
    Gateway gateway = gateway([room([point([actualDeviceConfig]), point([referencingDeviceConfig])])])

    when:
    def dereferencedDeviceConfig = referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    dereferencedDeviceConfig.config.something == "actual device value"
  }

  def "should fail with exception when device references to non-existing device"() {
    given:
    DeviceConfig referencingDeviceConfig = new DeviceConfig("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], "non_existing_device")
    Gateway gateway = gateway([room([point([referencingDeviceConfig])])])

    when:
    referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    thrown(Gateway.UnknownDeviceIdException)
  }

  def "should fail with exception when device is REFERENCE, but it doesn't have deviceReferenceId specified"() {
    given:
    DeviceConfig referencingDeviceConfig = new DeviceConfig("referencing_device_id", "test name", DeviceType.REFERENCE, [], [:], [:], null)
    Gateway gateway = gateway([room([point([referencingDeviceConfig])])])

    when:
    referencingDeviceConfig.dereferenceIfNeeded(gateway)

    then:
    thrown(DeviceConfig.UnexpectedDeviceConfigurationException)
  }
}
