package com.mqgateway.core.device.emulatedswitch

import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import spock.lang.Specification
import spock.lang.Subject

class EmulatedSwitchButtonDeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  def factory = new EmulatedSwitchButtonDeviceFactory(ioProvider)

  def "should create emulated switch button"() {
    given:
    def deviceConfig = new DeviceConfiguration("emulated_button", "Emulated switch button", DeviceType.EMULATED_SWITCH,
                                               ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "emulated_button"
    device.type == DeviceType.EMULATED_SWITCH
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("emulated_button", "Emulated switch button", DeviceType.EMULATED_SWITCH)

    when:
    factory.create(deviceConfig)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }

}
