package com.mqgateway.core.device.factory

import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.device.RelayDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import spock.lang.Specification
import spock.lang.Subject

class RelayDeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  def factory = new RelayDeviceFactory(ioProvider)

  def "should create relay"() {
    given:
    def deviceConfiguration = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfiguration)

    then:
    device.id == "myRelay"
    device.type == DeviceType.RELAY
  }

}
