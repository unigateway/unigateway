package com.mqgateway.core.device.emulatedswitch

import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class EmulatedSwitchButtonDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new EmulatedSwitchButtonDeviceFactory(testGatewayFactory.ioProvider)

  def "should create emulated switch button"() {
    given:
    def deviceConfig = new DeviceConfiguration("emulated_button", "Emulated switch button", DeviceType.EMULATED_SWITCH,
                                               ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "emulated_button"
    device.type == DeviceType.EMULATED_SWITCH
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("emulated_button", "Emulated switch button", DeviceType.EMULATED_SWITCH)

    when:
    factory.create(deviceConfig, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }

}
