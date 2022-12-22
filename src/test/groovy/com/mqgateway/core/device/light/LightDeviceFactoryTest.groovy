package com.mqgateway.core.device.light

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.ReferenceDeviceNotFoundException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class LightDeviceFactoryTest extends Specification {

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  LightDeviceFactory factory = new LightDeviceFactory()

  def "should create light device"() {
    given:
    DeviceConfiguration config = new DeviceConfiguration("light_device", "Light 1", DeviceType.LIGHT, [:], [
      relay  : new InternalDeviceConfiguration("light_relay"),
      switch1: new InternalDeviceConfiguration("switch_1"),
      switch2: new InternalDeviceConfiguration("switch_2"),
    ])
    Set<Device> devices = [testGatewayFactory.relayDevice("light_relay"),
                           testGatewayFactory.switchButtonDevice("switch_1"),
                           testGatewayFactory.switchButtonDevice("switch_2")].toSet()

    when:
    def lightDevice = factory.create(config, devices)

    then:
    lightDevice.id == "light_device"
    lightDevice.name == "Light 1"
    lightDevice.type == DeviceType.LIGHT
  }

  def "should throw exception when referenced 'relay' device is not on the devices list already"() {
    given:
    DeviceConfiguration config = new DeviceConfiguration("light_device", "Light 1", DeviceType.LIGHT, [:], [
      relay  : new InternalDeviceConfiguration("light_relay"),
      switch1: new InternalDeviceConfiguration("switch_1")
    ])
    Set<Device> devices = [testGatewayFactory.switchButtonDevice("switch_1")].toSet()

    when:
    factory.create(config, devices)

    then:
    def exception = thrown(ReferenceDeviceNotFoundException)
    exception.message == "Reference device not found (relay:light_relay) for device: light_device"
  }
}
