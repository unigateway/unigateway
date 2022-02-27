package com.mqgateway.core.device.factory


import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import spock.lang.Specification
import spock.lang.Subject

class SwitchButtonDeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  def factory = new SwitchButtonDeviceFactory(ioProvider)

  def "should create switch button"() {
    given:
    def deviceConfig = new DeviceConfiguration("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "mySwitchButton"
    device.type == DeviceType.SWITCH_BUTTON
  }

}
