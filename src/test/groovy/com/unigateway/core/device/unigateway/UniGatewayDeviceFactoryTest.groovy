package com.unigateway.core.device.unigateway

import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.device.DeviceType
import com.unigateway.core.utils.FakeSystemInfoProvider
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import spock.lang.Specification
import spock.lang.Subject

class UniGatewayDeviceFactoryTest extends Specification {

  @Subject
  def factory = new UniGatewayDeviceFactory(new FakeSystemInfoProvider())

  def "should create Unigateway as a device"() {
    given:
    def deviceConfig = new DeviceConfiguration("id", "name", DeviceType.UNIGATEWAY)

    when:
    def device = factory.create(deviceConfig, [] as Set)

    then:
    device.id == "id"
    device.type == DeviceType.UNIGATEWAY
  }
}
