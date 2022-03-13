package com.mqgateway.core.device.unigateway

import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.utils.UpdateListenerStub
import java.time.Duration
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class UniGatewayDeviceTest extends Specification {

  FakeSystemInfoProvider systemInfoProvider = new FakeSystemInfoProvider()
  UniGatewayDevice device = new UniGatewayDevice("unigateway-test-id", "Unigateway name", Duration.ofMillis(100), systemInfoProvider)
  UpdateListenerStub listenerStub = new UpdateListenerStub()
  PollingConditions conditions = new PollingConditions()

  def "should notify about UniGateway"() {
    given:
    systemInfoProvider.with {
      it.cpuTemperature = 130.21
      it.memoryFree = 245
      it.uptime = Duration.ofSeconds(781)
      it.ipAddresses = "1.1.1.1, 2.2.2.2"
    }
    device.addListener(listenerStub)

    when:
    device.init()

    then:
    conditions.eventually {
      assert listenerStub.receivedUpdates.find { it.propertyId == "temperature" }
        == new UpdateListenerStub.Update("unigateway-test-id", "temperature", "130.21")
      assert listenerStub.receivedUpdates.find { it.propertyId == "memory" }
        == new UpdateListenerStub.Update("unigateway-test-id", "memory", "245")
      assert listenerStub.receivedUpdates.find { it.propertyId == "uptime" }
        == new UpdateListenerStub.Update("unigateway-test-id", "uptime", "781")
      assert listenerStub.receivedUpdates.find { it.propertyId == "ip_address" }
        == new UpdateListenerStub.Update("unigateway-test-id", "ip_address", "1.1.1.1, 2.2.2.2")
    }

  }
}

