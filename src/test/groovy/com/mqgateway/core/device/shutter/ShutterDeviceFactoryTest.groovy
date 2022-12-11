package com.mqgateway.core.device.shutter

import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.ReferenceDeviceNotFoundException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import com.mqgateway.utils.TestGatewayFactory
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

  def "should pass configuration to shutter device when any config entries are set on configuration"() {
    given:
    def config = new DeviceConfiguration("shutter_device", "Shutter device", DeviceType.SHUTTER, [:], [
      stopRelay  : new InternalDeviceConfiguration("stop_relay"),
      upDownRelay: new InternalDeviceConfiguration("up_down_relay")
    ], [fullOpenTimeMs: "100", fullCloseTimeMs: "100", someConfig: "someValue"])
    def devices = [gatewayFactory.relayDevice("stop_relay"), gatewayFactory.relayDevice("up_down_relay")] as Set

    when:
    def shutterDevice = factory.create(config, devices)

    then:
    shutterDevice.id == "shutter_device"
    shutterDevice.name == "Shutter device"
    shutterDevice.type == DeviceType.SHUTTER
    shutterDevice.config == [fullOpenTimeMs: "100", fullCloseTimeMs: "100", someConfig: "someValue"]
  }
}
