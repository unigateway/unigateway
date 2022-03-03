package com.mqgateway.core.device.unigateway

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.utils.FakeSystemInfoProvider
import spock.lang.Specification
import spock.lang.Subject

class UniGatewayDeviceFactoryTest extends Specification {

  @Subject
  def factory = new UniGatewayDeviceFactory(new FakeSystemInfoProvider())

  def "should create MqGateway as a device"() {
    given:
    def deviceConfig = new DeviceConfiguration("id", "name", DeviceType.UNIGATEWAY)

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "id"
    device.type == DeviceType.UNIGATEWAY
  }
}
