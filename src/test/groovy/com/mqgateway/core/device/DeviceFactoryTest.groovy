package com.mqgateway.core.device

import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

class DeviceFactoryTest extends Specification {

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(), new MySensorsInputOutputProvider())

  @Subject
  DeviceFactory deviceFactory = new DeviceFactory(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())

  def "should create relay"() {
    given:
    def relayDeviceConfig = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, ["state": new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([relayDeviceConfig])

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof RelayDevice
    device.id == "myRelay"
    device.type == DeviceType.RELAY
  }

  def "should create switch button"() {
    given:
    def deviceConfig = new DeviceConfiguration("mySwitchButton", "Test switchButton", DeviceType.SWITCH_BUTTON, ["state": new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([deviceConfig])

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    Device device = devices.last()
    device instanceof SwitchButtonDevice
    device.id == "mySwitchButton"
    device.type == DeviceType.SWITCH_BUTTON
  }

  def "should create reed switch"() {
    given:
    def deviceConfig = new DeviceConfiguration("myReedSwitch", "Test ReedSwitch", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([deviceConfig])

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof ReedSwitchDevice
    device.id == "myReedSwitch"
    device.type == DeviceType.REED_SWITCH
  }

  def "should create motion detector"() {
    given:
    def deviceConfig = new DeviceConfiguration("myMotionDetector", "Test MotionDetector", DeviceType.MOTION_DETECTOR, ["state": new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([deviceConfig])

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof MotionSensorDevice
    device.id == "myMotionDetector"
    device.type == DeviceType.MOTION_DETECTOR
  }

  def "should create timer switch"() {
    given:
    def timerSwitchDeviceConfig = new DeviceConfiguration("myTimerSwitch", "Test timer switch", DeviceType.TIMER_SWITCH, ["state": new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([timerSwitchDeviceConfig])

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof TimerSwitchRelayDevice
    device.id == "myTimerSwitch"
    device.type == DeviceType.TIMER_SWITCH
  }

  @Ignore("TODO #21")
  def "should create shutter"() {
    given:
    List<DeviceConfiguration> devicesConfiguration = [
      new DeviceConfiguration("stopRelay", "relay1", DeviceType.RELAY, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("upDownRelay", "relay2", DeviceType.RELAY, ["state": new SimulatedConnector(2)]),
      new DeviceConfiguration("myShutter", "Test shutter device", DeviceType.SHUTTER, [:], [
        stopRelay  : new InternalDeviceConfiguration("stopRelay"),
        upDownRelay: new InternalDeviceConfiguration("upDownRelay")
      ], [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"])
    ]
    GatewayConfiguration gateway = gateway(devicesConfiguration)

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof ShutterDevice
    device.id == "myShutter"
    device.type == DeviceType.SHUTTER
  }

  @Ignore("TODO #21")
  def "should create three buttons gate device when gate device configuration has three buttons configured"() {
    given:
    List<DeviceConfiguration> devicesConfiguration = [
      new DeviceConfiguration("stopButton", "es1", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("openButton", "es2", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(2)]),
      new DeviceConfiguration("closeButton", "es3", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(3)]),
      new DeviceConfiguration("closedReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(4)]),
      new DeviceConfiguration("myGate", "Test gate device", DeviceType.GATE, [:],
          [
            stopButton      : new InternalDeviceConfiguration("stopButton"),
            openButton      : new InternalDeviceConfiguration("openButton"),
            closeButton     : new InternalDeviceConfiguration("closeButton"),
            closedReedSwitch: new InternalDeviceConfiguration("closedReedSwitch")
          ])
    ]
    GatewayConfiguration gateway = gateway(devicesConfiguration)

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof ThreeButtonsGateDevice
    device.id == "myGate"
    device.type == DeviceType.GATE
  }

  @Ignore("TODO #21")
  def "should create single button gate device when gate device configuration has action button configured only"() {
    given:
    List<DeviceConfiguration> devicesConfiguration = [
      new DeviceConfiguration("actionButton", "es1", DeviceType.EMULATED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("openReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("closedReedSwitch", "reedSwitch2", DeviceType.REED_SWITCH, ["state": new SimulatedConnector(1)]),
      new DeviceConfiguration("myGateSingleButton", "Test gate device", DeviceType.GATE, [:],
            [
              actionButton    : new InternalDeviceConfiguration("actionButton"),
              openReedSwitch  : new InternalDeviceConfiguration("openReedSwitch"),
              closedReedSwitch: new InternalDeviceConfiguration("closedReedSwitch")
            ])
    ]
    GatewayConfiguration gateway = gateway(devicesConfiguration)

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.last()
    device instanceof SingleButtonsGateDevice
    device.id == "myGateSingleButton"
    device.type == DeviceType.GATE
  }

  def "should create UniGateway as a device"() {
    given:
    GatewayConfiguration gateway = gateway([])

    when:
    def devices = deviceFactory.createAll(gateway)

    then:
    def device = devices.first()
    device instanceof MqGatewayDevice
    device.id == "gtwName"
    device.type == DeviceType.MQGATEWAY
  }
}
