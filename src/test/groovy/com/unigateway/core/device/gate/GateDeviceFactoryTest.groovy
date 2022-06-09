package com.unigateway.core.device.gate

import com.unigateway.core.device.DeviceType
import com.unigateway.core.device.ReferenceDeviceNotFoundException
import com.unigateway.core.device.UnexpectedDeviceConfigurationException
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.InternalDeviceConfiguration
import com.unigateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class GateDeviceFactoryTest extends Specification {

  TestGatewayFactory gatewayFactory = new TestGatewayFactory()

  @Subject
  def factory = new GateDeviceFactory()

  def "should create three buttons gate device when gate device configuration has three buttons configured"() {
    given:
    def config = new DeviceConfiguration("my_gate", "Test gate device", DeviceType.GATE, [:], [
      stopButton      : new InternalDeviceConfiguration("stop_button"),
      openButton      : new InternalDeviceConfiguration("open_button"),
      closeButton     : new InternalDeviceConfiguration("close_button"),
      closedReedSwitch: new InternalDeviceConfiguration("closed_reed_switch")
    ])
    def devices = [
      gatewayFactory.emulatedSwitchButtonDevice("stop_button"),
      gatewayFactory.emulatedSwitchButtonDevice("open_button"),
      gatewayFactory.emulatedSwitchButtonDevice("close_button"),
      gatewayFactory.reedSwitchDevice("closed_reed_switch")
    ] as Set

    when:
    def device = factory.create(config, devices)

    then:
    device.id == "my_gate"
    device.name == "Test gate device"
    device.class == ThreeButtonsGateDevice.class
  }

  def "should create single button gate device when gate device configuration has action button configured only"() {
    given:
    def config = new DeviceConfiguration("my_gate", "Test gate device", DeviceType.GATE, [:], [
      actionButton    : new InternalDeviceConfiguration("action_button"),
      openReedSwitch  : new InternalDeviceConfiguration("open_reed_switch"),
      closedReedSwitch: new InternalDeviceConfiguration("closed_reed_switch")
    ])
    def devices = [
      gatewayFactory.emulatedSwitchButtonDevice("action_button"),
      gatewayFactory.reedSwitchDevice("open_reed_switch"),
      gatewayFactory.reedSwitchDevice("closed_reed_switch")
    ] as Set

    when:
    def device = factory.create(config, devices)

    then:
    device.id == "my_gate"
    device.name == "Test gate device"
    device.class == SingleButtonsGateDevice.class
  }

  def "should throw exception when referenced device is not created"() {
    given:
    def config = new DeviceConfiguration("my_gate", "Test gate device", DeviceType.GATE, [:], [
      actionButton    : new InternalDeviceConfiguration("action_button"),
      openReedSwitch  : new InternalDeviceConfiguration("open_reed_switch"),
      closedReedSwitch: new InternalDeviceConfiguration("closed_reed_switch")
    ])
    def devices = [
      gatewayFactory.emulatedSwitchButtonDevice("action_button"),
      gatewayFactory.reedSwitchDevice("open_reed_switch")
    ] as Set

    when:
    factory.create(config, devices)

    then:
    def exception = thrown(ReferenceDeviceNotFoundException)
    exception.message == "Reference device not found (closedReedSwitch:closed_reed_switch) for device: my_gate"
  }

  def "should throw exception when configuration does not match to create single or three button gate"() {
    given:
    def config = new DeviceConfiguration("my_gate", "Test gate device", DeviceType.GATE, [:], [
      closedReedSwitch: new InternalDeviceConfiguration("closed_reed_switch")
    ])
    def devices = [
      gatewayFactory.emulatedSwitchButtonDevice("action_button"),
      gatewayFactory.reedSwitchDevice("open_reed_switch")
    ] as Set

    when:
    factory.create(config, devices)

    then:
    def exception = thrown(UnexpectedDeviceConfigurationException)
    exception.message == "Unexpected device configuration for device: my_gate. Gate device should have either three buttons defined (stopButton, openButton, closeButton) or single (actionButton)"
  }

}
