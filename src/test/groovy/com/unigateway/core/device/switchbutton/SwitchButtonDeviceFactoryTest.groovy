package com.unigateway.core.device.switchbutton

import com.unigateway.core.device.MissingConnectorInDeviceConfigurationException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.unigateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.unigateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.io.provider.MySensorsInputOutputProvider
import com.unigateway.core.mysensors.MySensorsSerialConnection
import com.unigateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class SwitchButtonDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new SwitchButtonDeviceFactory(testGatewayFactory.ioProvider)

  def "should create switch button"() {
    given:
    def deviceConfig = new DeviceConfiguration("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "mySwitchButton"
    device.type == DeviceType.SWITCH_BUTTON
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON)

    when:
    factory.create(deviceConfig, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }
}
