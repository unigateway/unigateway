package com.mqgateway.core.device.factory

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.utils.FakeSystemInfoProvider
import spock.lang.Specification
import spock.lang.Subject

class MqGatewayDeviceFactoryTest extends Specification {

  @Subject
  def factory = new MqGatewayDeviceFactory(new FakeSystemInfoProvider())

  def "should create MqGateway as a device"() {
    given:
    def deviceConfig = new DeviceConfiguration("id", "name", DeviceType.MQGATEWAY)

    when:
    def device = factory.create(deviceConfig)

    then:
    device.id == "id"
    device.type == DeviceType.MQGATEWAY
  }
}
