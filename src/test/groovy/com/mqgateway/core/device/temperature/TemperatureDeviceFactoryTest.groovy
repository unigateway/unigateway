package com.mqgateway.core.device.temperature

import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.io.provider.MySensorsConnector
import com.mqgateway.core.mysensors.SetReqType
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class TemperatureDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new TemperatureDeviceFactory(testGatewayFactory.ioProvider)

  def "should create humidity device"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("id", "Test device", DeviceType.TEMPERATURE, ["state": new MySensorsConnector(1, 1, SetReqType.V_TEMP)])

    when:
    def device = factory.create(deviceConfiguration, [] as Set)

    then:
    device.id == "id"
    device.type == DeviceType.TEMPERATURE
    device.properties.find { it.name() == "temperature" }.unit == DataUnit.CELSIUS
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("id", "Test device", DeviceType.TEMPERATURE, ["not-state": new MySensorsConnector(1, 1, SetReqType.V_TEMP)])

    when:
    factory.create(deviceConfiguration, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }
}
