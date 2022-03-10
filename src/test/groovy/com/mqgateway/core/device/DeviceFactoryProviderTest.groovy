package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import spock.lang.Specification
import spock.lang.Subject

class DeviceFactoryProviderTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(new SimulatedPlatformConfiguration("someValue")),
                                                           new MySensorsInputOutputProvider())

  @Subject
  DeviceFactoryProvider provider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  def "should return factory for device type: #deviceType"(DeviceType deviceType) {
    expect:
    provider.getFactory(deviceType) != null

    where:
    deviceType << DeviceType.values()
  }
}
