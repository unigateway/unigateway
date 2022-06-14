package com.mqgateway.core.device.relay

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

class RelayDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new RelayDeviceFactory(testGatewayFactory.ioProvider)

  def "should create relay"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfiguration, [] as Set)

    then:
    device.id == "myRelay"
    device.type == DeviceType.RELAY
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY)

    when:
    factory.create(deviceConfiguration, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }
}
