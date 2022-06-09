package com.mqgateway.homie.gateway

import static com.mqgateway.core.device.DevicePropertyType.STATE
import static com.mqgateway.core.device.DevicePropertyType.TIMER
import static com.mqgateway.homie.HomieProperty.DataType.ENUM
import static com.mqgateway.homie.HomieProperty.DataType.FLOAT
import static com.mqgateway.homie.HomieProperty.DataType.INTEGER
import static com.mqgateway.homie.HomieProperty.DataType.STRING
import static com.mqgateway.homie.HomieProperty.Unit.CELSIUS
import static com.mqgateway.homie.HomieProperty.Unit.NONE

import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceRegistryFactory
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.mysensors.MySensorsSerialConnection
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.homie.HomieReceiver
import com.mqgateway.homie.HomieReceiverStub
import com.mqgateway.utils.MqttClientFactoryStub
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class HomieDeviceFactoryTest extends Specification {

  MqttClientFactoryStub mqttClientFactoryStub = new MqttClientFactoryStub()
  HomieReceiver homieReceiver = new HomieReceiverStub()
  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()
  DeviceFactoryProvider deviceFactoryProvider = new DeviceFactoryProvider(testGatewayFactory.ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())
  DeviceRegistryFactory deviceRegistryFactory = new DeviceRegistryFactory(deviceFactoryProvider)

  @Subject
  HomieDeviceFactory homieDeviceFactory = new HomieDeviceFactory(mqttClientFactoryStub, homieReceiver, "test-version")

  def "should create HomieDevice with nodes and properties based on gateway configuration"() {
    given:
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      new DeviceConfiguration("device1", "device1 name", DeviceType.RELAY, [state: new SimulatedConnector(1)])
    ])
    DeviceRegistry deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def homieDevice = homieDeviceFactory.toHomieDevice(deviceRegistry, "ethXXX")

    then:
    homieDevice.id == "unigateway-id"
    homieDevice.name == "Gateway name"
    homieDevice.nodes.size() == 2
    homieDevice.nodes["device1"] == new HomieNode("unigateway-id", "device1", "device1 name", "relay", [
      state: new HomieProperty("unigateway-id", "device1", "state", "state", ENUM, "ON,OFF", true, true, NONE)
    ])
    homieDevice.nodes["unigateway-id"] == new HomieNode("unigateway-id", "unigateway-id", "Gateway name", "unigateway", [
      temperature: new HomieProperty("unigateway-id", "unigateway-id", "temperature", "temperature", FLOAT, null, false, true, CELSIUS),
      memory     : new HomieProperty("unigateway-id", "unigateway-id", "memory", "memory", INTEGER, null, false, true, NONE),
      uptime     : new HomieProperty("unigateway-id", "unigateway-id", "uptime", "uptime", INTEGER, null, false, true, NONE),
      ip_address : new HomieProperty("unigateway-id", "unigateway-id", "ip_address", "ip_address", STRING, null, false, true, NONE)
    ])
  }

  def "should create HomieProperties for Relay"() {
    given:
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      new DeviceConfiguration("relay_in_test", "Test Relay", DeviceType.RELAY, [state: new SimulatedConnector(1)])
    ])
    DeviceRegistry deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def homieDevice = homieDeviceFactory.toHomieDevice(deviceRegistry, "ethXXX")

    then:
    def node = homieDevice.nodes["relay_in_test"]
    node.properties.keySet() == [STATE.toString()].toSet()
    node.properties[STATE.toString()] == new HomieProperty("unigateway-id", "relay_in_test", "state", "state", ENUM, "ON,OFF", true, true, NONE)
  }

  def "should create HomieProperties for MotionDetector"() {
    given:
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      new DeviceConfiguration("motiondetector_in_test", "Motion Detector", DeviceType.MOTION_DETECTOR, [state: new SimulatedConnector(1)])
    ])
    DeviceRegistry deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def homieDevice = homieDeviceFactory.toHomieDevice(deviceRegistry, "ethXXX")

    then:
    def node = homieDevice.nodes["motiondetector_in_test"]
    node.properties.keySet() == [STATE.toString()].toSet()
    node.properties[STATE.toString()] == new HomieProperty("unigateway-id", "motiondetector_in_test", "state", "state", ENUM, "ON,OFF", false, true, NONE)
  }

  def "should create HomieProperties for SwitchButton"() {
    given:
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      new DeviceConfiguration("switchButton_in_test", "Switch Button", DeviceType.SWITCH_BUTTON, [state: new SimulatedConnector(1)])
    ])
    DeviceRegistry deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def homieDevice = homieDeviceFactory.toHomieDevice(deviceRegistry, "ethXXX")

    then:
    def node = homieDevice.nodes["switchButton_in_test"]
    node.properties.keySet() == [STATE.toString()].toSet()
    node.properties[STATE.toString()] == new HomieProperty("unigateway-id", "switchButton_in_test", "state", "state", ENUM, "PRESSED,RELEASED", false, false, NONE)
  }

  def "should create HomieProperties for ReedSwitch"() {
    given:
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      new DeviceConfiguration("reedSwitch_in_test", "Reed Switch", DeviceType.REED_SWITCH, [state: new SimulatedConnector(1)])
    ])
    DeviceRegistry deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def homieDevice = homieDeviceFactory.toHomieDevice(deviceRegistry, "ethXXX")

    then:
    def node = homieDevice.nodes["reedSwitch_in_test"]
    node.properties.keySet() == [STATE.toString()].toSet()
    node.properties[STATE.toString()] == new HomieProperty("unigateway-id", "reedSwitch_in_test", "state", "state", ENUM, "OPEN,CLOSED", false, true, NONE)
  }

  def "should create HomieProperties for TimerSwitch"() {
    given:
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      new DeviceConfiguration("timerswitch_in_test", "Test Timer Switch", DeviceType.TIMER_SWITCH, [state: new SimulatedConnector(1)])
    ])
    DeviceRegistry deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def homieDevice = homieDeviceFactory.toHomieDevice(deviceRegistry, "ethXXX")

    then:
    def node = homieDevice.nodes["timerswitch_in_test"]
    node.properties.keySet() == [STATE.toString(), TIMER.toString()].toSet()
    node.properties[STATE.toString()] == new HomieProperty("unigateway-id", "timerswitch_in_test", "state", "state", ENUM, "ON,OFF", false, true, NONE)
    node.properties[TIMER.toString()] == new HomieProperty("unigateway-id", "timerswitch_in_test", "timer", "timer", INTEGER, "0:1440", true, true, NONE)
  }
}


