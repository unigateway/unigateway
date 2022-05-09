package com.mqgateway.core.device.reedswitch

import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.mysensors.MySensorsSerialConnection
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class ReedSwitchDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new ReedSwitchDeviceFactory(testGatewayFactory.ioProvider)

  def "should create reed switch"() {
    given:
    def deviceConfig = new DeviceConfiguration("myReedSwitch", "Test ReedSwitch", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "myReedSwitch"
    device.type == DeviceType.REED_SWITCH
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("myReedSwitch", "Test ReedSwitch", DeviceType.REED_SWITCH)

    when:
    factory.create(deviceConfig, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }

}
