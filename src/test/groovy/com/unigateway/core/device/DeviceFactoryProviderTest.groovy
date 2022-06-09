package com.unigateway.core.device

import com.unigateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.unigateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.unigateway.core.io.provider.DisabledMySensorsInputOutputProvider
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.utils.FakeSystemInfoProvider
import com.unigateway.core.utils.TimersScheduler
import com.unigateway.core.io.provider.DisabledMySensorsInputOutputProvider
import spock.lang.Specification
import spock.lang.Subject

class DeviceFactoryProviderTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(new SimulatedPlatformConfiguration("someValue")),
                                                           new DisabledMySensorsInputOutputProvider())

  @Subject
  DeviceFactoryProvider provider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  def "should return factory for device type: #deviceType"(DeviceType deviceType) {
    expect:
    provider.getFactory(deviceType) != null

    where:
    deviceType << DeviceType.values()
  }
}
