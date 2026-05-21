package com.mqgateway.core.device.buzzer

import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class BuzzerDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new BuzzerDeviceFactory(testGatewayFactory.ioProvider)

  def "should create buzzer device"() {
    given:
    def deviceConfig = new DeviceConfiguration("myBuzzer", "Test buzzer", DeviceType.BUZZER, [state: new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "myBuzzer"
    device.type == DeviceType.BUZZER
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("myBuzzer", "Test buzzer", DeviceType.BUZZER)

    when:
    factory.create(deviceConfig, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }
}
