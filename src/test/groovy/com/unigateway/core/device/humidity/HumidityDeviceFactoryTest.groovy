package com.unigateway.core.device.humidity

import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.MissingConnectorInDeviceConfigurationException
import com.unigateway.core.device.relay.RelayDeviceFactory
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.io.provider.MySensorsConnector
import com.unigateway.core.mysensors.SetReqType
import com.unigateway.utils.TestGatewayFactory
import com.unigateway.core.mysensors.SetReqType
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
