package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import spock.lang.Specification
import spock.lang.Subject

class DeviceRegistryFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(new SimulatedPlatformConfiguration("someValue")),
                                                           new MySensorsInputOutputProvider())
  def deviceFactoryProvider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  @Subject
  def factory = new DeviceRegistryFactory(deviceFactoryProvider)

  def "should register UniGateway device"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("unigateway-id").type == DeviceType.UNIGATEWAY
  }

  def "should register devices from configuration"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [
      new DeviceConfiguration("relay_device_1", "Relay device", DeviceType.RELAY, [state: new SimulatedConnector(10)])
    ])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("relay_device_1").type == DeviceType.RELAY
  }

  def "should create shutter device"() {
    given:
    def config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [
      new DeviceConfiguration("shutter_device", "Shutter device", DeviceType.SHUTTER, [:], [
        stopRelay  : new InternalDeviceConfiguration("stop_relay"),
        upDownRelay: new InternalDeviceConfiguration("up_down_relay")
      ], [fullOpenTimeMs: "100", fullCloseTimeMs: "100"]),
      new DeviceConfiguration("stop_relay", "Stop relay device", DeviceType.RELAY, [state: new SimulatedConnector(1)]),
      new DeviceConfiguration("up_down_relay", "Up down relay device", DeviceType.RELAY, [state: new SimulatedConnector(2)])
    ])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("stop_relay").type == DeviceType.RELAY
    registry.getById("up_down_relay").type == DeviceType.RELAY
    registry.getById("shutter_device").type == DeviceType.SHUTTER
  }

  def "should create three buttons gate device when gate device configuration has three buttons configured"() {
    given:
    GatewayConfiguration config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [
      new DeviceConfiguration("myGate", "Test gate device", DeviceType.GATE, [:],
                              [
                                stopButton      : new InternalDeviceConfiguration("stopButton"),
                                openButton      : new InternalDeviceConfiguration("openButton"),
                                closeButton     : new InternalDeviceConfiguration("closeButton"),
                                closedReedSwitch: new InternalDeviceConfiguration("closedReedSwitch")
                              ]),
      new DeviceConfiguration("stopButton", "es1", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("openButton", "es2", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(2)]),
      new DeviceConfiguration("closeButton", "es3", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(3)]),
      new DeviceConfiguration("closedReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(4)])
    ])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("myGate").type == DeviceType.GATE
    registry.getById("stopButton").type == DeviceType.EMULATED_SWITCH
    registry.getById("openButton").type == DeviceType.EMULATED_SWITCH
    registry.getById("closeButton").type == DeviceType.EMULATED_SWITCH
    registry.getById("closedReedSwitch").type == DeviceType.REED_SWITCH
  }

  def "should create single button gate device when gate device configuration has action button configured only"() {
    given:
    GatewayConfiguration config = new GatewayConfiguration("1.0.0", "unigateway-id", "UniGateway", [
      new DeviceConfiguration("myGateSingleButton", "Test gate device", DeviceType.GATE, [:],
                              [
                                actionButton    : new InternalDeviceConfiguration("actionButton"),
                                openReedSwitch  : new InternalDeviceConfiguration("openReedSwitch"),
                                closedReedSwitch: new InternalDeviceConfiguration("closedReedSwitch")
                              ]),
      new DeviceConfiguration("actionButton", "es1", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("openReedSwitch", "es2", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(2)]),
      new DeviceConfiguration("closedReedSwitch", "es3", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(3)])
    ])

    when:
    def registry = factory.create(config)

    then:
    registry.getById("myGateSingleButton").type == DeviceType.GATE
    registry.getById("actionButton").type == DeviceType.EMULATED_SWITCH
    registry.getById("openReedSwitch").type == DeviceType.REED_SWITCH
    registry.getById("closedReedSwitch").type == DeviceType.REED_SWITCH
  }

}
