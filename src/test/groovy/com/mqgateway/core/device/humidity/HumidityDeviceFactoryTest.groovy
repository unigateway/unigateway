package com.mqgateway.core.device.humidity

import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.DevicePropertyType
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.io.provider.MySensorsConnector
import com.mqgateway.core.mysensors.SetReqType
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class HumidityDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new HumidityDeviceFactory(testGatewayFactory.ioProvider)

  def "should create humidity device"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("id", "Test device", DeviceType.HUMIDITY, ["state": new MySensorsConnector(1, 1, SetReqType.V_HUM)])

    when:
    def device = factory.create(deviceConfiguration, [] as Set)

    then:
    device.id == "id"
    device.type == DeviceType.HUMIDITY
    device.properties.find { it.name() == DevicePropertyType.HUMIDITY.toString() }.unit == DataUnit.PERCENT
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("id", "Test device", DeviceType.HUMIDITY, ["not-state": new MySensorsConnector(1, 1, SetReqType.V_HUM)])

    when:
    factory.create(deviceConfiguration, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }
}
