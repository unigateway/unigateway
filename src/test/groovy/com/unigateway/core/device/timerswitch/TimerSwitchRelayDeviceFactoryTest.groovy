package com.unigateway.core.device.timerswitch

import com.unigateway.core.device.MissingConnectorInDeviceConfigurationException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.unigateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.unigateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.io.provider.MySensorsInputOutputProvider
import com.unigateway.core.utils.TimersScheduler
import com.unigateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class TimerSwitchRelayDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new TimerSwitchRelayDeviceFactory(testGatewayFactory.ioProvider, new TimersScheduler())

  def "should create timer switch"() {
    given:
    def deviceConfig = new DeviceConfiguration("myTimerSwitch", "Test timer switch", DeviceType.TIMER_SWITCH, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "myTimerSwitch"
    device.type == DeviceType.TIMER_SWITCH
  }

  def "should throw exception when state connector configuration is not provided"() {
    given:
    def deviceConfig = new DeviceConfiguration("myTimerSwitch", "Test timer switch", DeviceType.TIMER_SWITCH)

    when:
    factory.create(deviceConfig, [] as Set)

    then:
    thrown(MissingConnectorInDeviceConfigurationException)
  }
}
