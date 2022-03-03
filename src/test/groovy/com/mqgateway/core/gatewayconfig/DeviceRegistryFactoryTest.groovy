package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import spock.lang.Specification
import spock.lang.Subject

class DeviceRegistryFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())
  def deviceFactoryProvider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  @Subject
  def factory = new DeviceRegistryFactory(deviceFactoryProvider)

  def "should register UniGateway device"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("unigateway-id").type == DeviceType.UNIGATEWAY
  }

  def "should register devices from configuration"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [
      new DeviceConfiguration("relay_device_1", "Relay device", DeviceType.RELAY, [state: new SimulatedConnector(10)])
    ])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("relay_device_1").type == DeviceType.RELAY
  }
}
