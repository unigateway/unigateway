package com.mqgateway.core.device.timerswitch


import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.TimersScheduler
import spock.lang.Specification
import spock.lang.Subject

class TimerSwitchRelayDeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  def factory = new TimerSwitchRelayDeviceFactory(ioProvider, new TimersScheduler())

  def "should create timer switch"() {
    given:
    def deviceConfig = new DeviceConfiguration("myTimerSwitch", "Test timer switch", DeviceType.TIMER_SWITCH, ["state": new SimulatedConnector(1)])

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "myTimerSwitch"
    device.type == DeviceType.TIMER_SWITCH
  }

}
