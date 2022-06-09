package com.unigateway.core.device.shutter

import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.ReferenceDeviceNotFoundException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.InternalDeviceConfiguration
import com.unigateway.utils.TestGatewayFactory
import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.ReferenceDeviceNotFoundException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.InternalDeviceConfiguration
import spock.lang.Specification
import spock.lang.Subject

class ShutterDeviceFactoryTest extends Specification {

  TestGatewayFactory gatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new ShutterDeviceFactory()

  def "should create shutter device"() {
    given:
    def config = new DeviceConfiguration("shutter_device", "Shutter device", DeviceType.SHUTTER, [:], [
      stopRelay  : new InternalDeviceConfiguration("stop_relay"),
      upDownRelay: new InternalDeviceConfiguration("up_down_relay")
    ], [fullOpenTimeMs: "100", fullCloseTimeMs: "100"])
    def devices = [gatewayFactory.relayDevice("stop_relay"), gatewayFactory.relayDevice("up_down_relay")] as Set

    when:
    def shutterDevice = factory.create(config, devices)

    then:
    shutterDevice.id == "shutter_device"
    shutterDevice.name == "Shutter device"
    shutterDevice.type == DeviceType.SHUTTER
  }

  def "should throw exception when stop_relay is not created"() {
    given:
    def config = new DeviceConfiguration("shutter_device", "Shutter device", DeviceType.SHUTTER, [:], [
      stopRelay  : new InternalDeviceConfiguration("stop_relay"),
      upDownRelay: new InternalDeviceConfiguration("up_down_relay")
    ], [fullOpenTimeMs: "100", fullCloseTimeMs: "100"])
    def devices = [gatewayFactory.relayDevice("up_down_relay")] as Set

    when:
    factory.create(config, devices)

    then:
    def exception = thrown(ReferenceDeviceNotFoundException)
    exception.message == "Reference device not found (stopRelay:stop_relay) for device: shutter_device"
  }

  def "should throw exception when up_down_relay is not created"() {
    given:
    def config = new DeviceConfiguration("shutter_device", "Shutter device", DeviceType.SHUTTER, [:], [
      stopRelay  : new InternalDeviceConfiguration("stop_relay"),
      upDownRelay: new InternalDeviceConfiguration("up_down_relay")
    ], [fullOpenTimeMs: "100", fullCloseTimeMs: "100"])
    def devices = [gatewayFactory.relayDevice("stop_relay")] as Set

    when:
    factory.create(config, devices)

    then:
    def exception = thrown(ReferenceDeviceNotFoundException)
    exception.message == "Reference device not found (upDownRelay:up_down_relay) for device: shutter_device"
  }

}
