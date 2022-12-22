package com.mqgateway.core.gatewayconfig.homeassistant

import static com.mqgateway.core.device.DevicePropertyType.IP_ADDRESS
import static com.mqgateway.core.device.DevicePropertyType.MEMORY
import static com.mqgateway.core.device.DevicePropertyType.POSITION
import static com.mqgateway.core.device.DevicePropertyType.STATE
import static com.mqgateway.core.device.DevicePropertyType.TEMPERATURE
import static com.mqgateway.core.device.DevicePropertyType.UPTIME
import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceRegistryFactory
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.InternalDeviceConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConverterTest extends Specification {

  String firmwareVersion = "1.2.3-TEST-ONLY"

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()

  @Subject
  HomeAssistantConverter converter = new HomeAssistantConverter(firmwareVersion)

  DeviceFactoryProvider deviceFactoryProvider = new DeviceFactoryProvider(testGatewayFactory.ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())
  DeviceRegistryFactory deviceRegistryFactory = new DeviceRegistryFactory(deviceFactoryProvider)

  def "should convert MqGateway relay to HA light when set explicitly in gateway configuration"() {
    given:
    def relayDeviceConfig = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, [state: new SimulatedConnector(1)],
                                                    [:], ["haComponent": "light"])
    GatewayConfiguration gateway = new GatewayConfiguration("1.0", "unigateway-id", "Gateway name", [
      relayDeviceConfig
    ])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantLight lightComponent = components[0] as HomeAssistantLight
    lightComponent.componentType == HomeAssistantComponentType.LIGHT
    lightComponent.name == "Test relay"
    lightComponent.properties.nodeId == gateway.id
    lightComponent.properties.objectId == "myRelay"
    lightComponent.stateTopic == expectedStateTopic(gateway.id, relayDeviceConfig.id, STATE.toString())
    lightComponent.commandTopic == expectedCommandTopic(gateway.id, relayDeviceConfig.id, STATE.toString())
    lightComponent.retain
    lightComponent.payloadOn == "ON"
    lightComponent.payloadOff == "OFF"
    lightComponent.uniqueId == gateway.id + "_" + relayDeviceConfig.id
    assertHomeAssistantDevice(lightComponent, gateway, relayDeviceConfig)
  }

  def "should convert MqGateway relay to HA switch when haComponent not set explicitly in gateway configuration"() {
    given:
    def relayDeviceConfig = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, [state: new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([relayDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantSwitch switchComponent = components[0] as HomeAssistantSwitch
    switchComponent.componentType == HomeAssistantComponentType.SWITCH
    switchComponent.name == "Test relay"
    switchComponent.properties.nodeId == gateway.id
    switchComponent.properties.objectId == "myRelay"
    switchComponent.stateTopic == expectedStateTopic(gateway.id, relayDeviceConfig.id, STATE.toString())
    switchComponent.commandTopic == expectedCommandTopic(gateway.id, relayDeviceConfig.id, STATE.toString())
    switchComponent.retain
    switchComponent.payloadOn == "ON"
    switchComponent.payloadOff == "OFF"
    switchComponent.uniqueId == gateway.id + "_" + relayDeviceConfig.id
    assertHomeAssistantDevice(switchComponent, gateway, relayDeviceConfig)
  }

  def "should convert MqGateway SWITCH_BUTTON to 4 HA triggers when haComponent is set to 'device_automation'"() {
    given:
    def switchButtonDeviceConfig = new DeviceConfiguration("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON,
                                                           [state: new SimulatedConnector(1)], [:], [haComponent: "device_automation"])
    GatewayConfiguration gateway = gateway([switchButtonDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 4
    HomeAssistantTrigger pressedTrigger = components.find { it.payload == "PRESSED" } as HomeAssistantTrigger
    HomeAssistantTrigger releasedTrigger = components.find { it.payload == "RELEASED" } as HomeAssistantTrigger
    HomeAssistantTrigger longPressedTrigger = components.find { it.payload == "LONG_PRESSED" } as HomeAssistantTrigger
    HomeAssistantTrigger longReleasedTrigger = components.find { it.payload == "LONG_RELEASED" } as HomeAssistantTrigger

    components.each { HomeAssistantComponent component ->
      HomeAssistantTrigger triggerComponent = component as HomeAssistantTrigger
      assertHomeAssistantDevice(component, gateway, switchButtonDeviceConfig)
      assert triggerComponent.componentType == HomeAssistantComponentType.TRIGGER
      assert triggerComponent.properties.nodeId == gateway.id
      assert triggerComponent.topic == expectedStateTopic(gateway.id, switchButtonDeviceConfig.id, STATE.toString())
      assert triggerComponent.subtype == "button"
    }

    pressedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_SHORT_PRESS
    pressedTrigger.properties.objectId == "mySwitchButton_PRESS"
    releasedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_SHORT_RELEASE
    releasedTrigger.properties.objectId == "mySwitchButton_RELEASE"
    longPressedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_LONG_PRESS
    longPressedTrigger.properties.objectId == "mySwitchButton_LONG_PRESS"
    longReleasedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_LONG_RELEASE
    longReleasedTrigger.properties.objectId == "mySwitchButton_LONG_RELEASE"
  }

  def "should convert MqGateway SWITCH_BUTTON to sensor when haComponent is set to 'sensor'"() {
    given:
    def switchButtonDeviceConfig = new DeviceConfiguration("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON,
                                                           [state: new SimulatedConnector(1)], [:], [haComponent: "sensor"])
    GatewayConfiguration gateway = gateway([switchButtonDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantSensor sensorComponent = components[0] as HomeAssistantSensor
    sensorComponent.componentType == HomeAssistantComponentType.SENSOR
    sensorComponent.name == "Test button"
    sensorComponent.properties.nodeId == gateway.id
    sensorComponent.properties.objectId == "mySwitchButton"
    sensorComponent.stateTopic == expectedStateTopic(gateway.id, switchButtonDeviceConfig.id, STATE.toString())
    sensorComponent.unitOfMeasurement == null
    sensorComponent.deviceClass == HomeAssistantSensor.DeviceClass.NONE
    sensorComponent.uniqueId == gateway.id + "_" + switchButtonDeviceConfig.id
    assertHomeAssistantDevice(sensorComponent, gateway, switchButtonDeviceConfig)
  }

  def "should convert MqGateway SWITCH_BUTTON to binary sensor when haComponent is not set"() {
    given:
    def switchButtonDeviceConfig = new DeviceConfiguration("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON,
                                                           [state: new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([switchButtonDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
    binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
    binarySensorComponent.name == "Test button"
    binarySensorComponent.properties.nodeId == gateway.id
    binarySensorComponent.properties.objectId == switchButtonDeviceConfig.id
    binarySensorComponent.stateTopic == expectedStateTopic(gateway.id, switchButtonDeviceConfig.id, STATE.toString())
    binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.NONE
    binarySensorComponent.uniqueId == "${gateway.id}_${switchButtonDeviceConfig.id}"
    binarySensorComponent.payloadOn == "PRESSED"
    binarySensorComponent.payloadOff == "RELEASED"
    binarySensorComponent.uniqueId == gateway.id + "_" + switchButtonDeviceConfig.id
    assertHomeAssistantDevice(binarySensorComponent, gateway, switchButtonDeviceConfig)
  }

  def "should convert MqGateway REED_SWITCH to HA binary sensor"() {
    given:
    def reedSwitchDeviceConfig = new DeviceConfiguration("myReedSwitch", "Test reed switch", DeviceType.REED_SWITCH,
                                                         [state: new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([reedSwitchDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
    binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
    binarySensorComponent.name == "Test reed switch"
    binarySensorComponent.properties.nodeId == gateway.id
    binarySensorComponent.properties.objectId == "myReedSwitch"
    binarySensorComponent.stateTopic == expectedStateTopic(gateway.id, reedSwitchDeviceConfig.id, STATE.toString())
    binarySensorComponent.payloadOn == "OPEN"
    binarySensorComponent.payloadOff == "CLOSED"
    binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.OPENING
    binarySensorComponent.uniqueId == gateway.id + "_" + reedSwitchDeviceConfig.id
    assertHomeAssistantDevice(binarySensorComponent, gateway, reedSwitchDeviceConfig)
  }

  def "should convert MqGateway MOTION_DETECTOR to HA binary sensor"() {
    given:
    def motionDetectorDeviceConfig = new DeviceConfiguration("myMotionDetector", "Test motion detector", DeviceType.MOTION_DETECTOR,
                                                             [state: new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([motionDetectorDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
    binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
    binarySensorComponent.name == "Test motion detector"
    binarySensorComponent.properties.nodeId == gateway.id
    binarySensorComponent.properties.objectId == "myMotionDetector"
    binarySensorComponent.stateTopic == expectedStateTopic(gateway.id, motionDetectorDeviceConfig.id, STATE.toString())
    binarySensorComponent.payloadOn == "ON"
    binarySensorComponent.payloadOff == "OFF"
    binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.MOTION
    binarySensorComponent.uniqueId == gateway.id + "_" + motionDetectorDeviceConfig.id
    assertHomeAssistantDevice(binarySensorComponent, gateway, motionDetectorDeviceConfig)
  }

  def "should convert MqGateway EMULATED_SWITCH to HA switch"() {
    given:
    def emulatedSwitchDeviceConfig = new DeviceConfiguration("myEmulatedSwitch", "Test emulated switch", DeviceType.EMULATED_SWITCH,
                                                             [state: new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([emulatedSwitchDeviceConfig])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 1
    HomeAssistantSwitch switchComponent = components[0] as HomeAssistantSwitch
    switchComponent.componentType == HomeAssistantComponentType.SWITCH
    switchComponent.name == "Test emulated switch"
    switchComponent.properties.nodeId == gateway.id
    switchComponent.properties.objectId == "myEmulatedSwitch"
    switchComponent.stateTopic == expectedStateTopic(gateway.id, emulatedSwitchDeviceConfig.id, STATE.toString())
    switchComponent.commandTopic == expectedCommandTopic(gateway.id, emulatedSwitchDeviceConfig.id, STATE.toString())
    !switchComponent.retain
    switchComponent.payloadOn == "PRESSED"
    switchComponent.payloadOff == "RELEASED"
    switchComponent.uniqueId == gateway.id + "_" + emulatedSwitchDeviceConfig.id
    assertHomeAssistantDevice(switchComponent, gateway, emulatedSwitchDeviceConfig)
  }

  def "should convert MqGateway SHUTTER to HA cover"() {
    given:
    def shutterDevice = new DeviceConfiguration("myShutter", "Test shutter device", DeviceType.SHUTTER, [:], [
      stopRelay  : new InternalDeviceConfiguration("stop_relay"),
      upDownRelay: new InternalDeviceConfiguration("up_down_relay")
    ], [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"])
    def stopRelay = new DeviceConfiguration("stop_relay", "Stop Relay", DeviceType.RELAY, [state: new SimulatedConnector(1)])
    def upDownRelay = new DeviceConfiguration("up_down_relay", "UpDown Relay", DeviceType.RELAY, [state: new SimulatedConnector(1)])
    GatewayConfiguration gateway = gateway([shutterDevice, stopRelay, upDownRelay])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 3
    HomeAssistantCover cover = components.find { it.componentType == HomeAssistantComponentType.COVER }
    cover.deviceClass == HomeAssistantCover.DeviceClass.SHUTTER
    cover.name == "Test shutter device"
    cover.properties.nodeId == gateway.id
    cover.properties.objectId == "myShutter"
    cover.stateTopic == expectedStateTopic(gateway.id, shutterDevice.id, STATE.toString())
    cover.commandTopic == expectedCommandTopic(gateway.id, shutterDevice.id, STATE.toString())
    !cover.retain
    cover.payloadClose == "CLOSE"
    cover.payloadOpen == "OPEN"
    cover.payloadStop == "STOP"
    cover.positionClosed == 0
    cover.positionOpen == 100
    cover.positionTopic == expectedStateTopic(gateway.id, shutterDevice.id, POSITION.toString())
    cover.setPositionTopic == expectedCommandTopic(gateway.id, shutterDevice.id, POSITION.toString())
    cover.uniqueId == gateway.id + "_" + shutterDevice.id
    cover.stateOpen == "OPEN"
    cover.stateClosed == "CLOSED"
    cover.stateOpening == "OPENING"
    cover.stateClosing == "CLOSING"
    cover.stateStopped == null
    assertHomeAssistantDevice(cover, gateway, shutterDevice)
  }

  def "should convert MqGateway GATE to HA cover"() {
    given:
    def gateDevice = new DeviceConfiguration("myGate", "Test gate device", DeviceType.GATE, [:], [
      stopButton      : new InternalDeviceConfiguration("stop_button"),
      openButton      : new InternalDeviceConfiguration("open_button"),
      closeButton     : new InternalDeviceConfiguration("close_button"),
      closedReedSwitch: new InternalDeviceConfiguration("closed_reed_switch")
    ], [haDeviceClass: "gate"])
    def stopEmulatedSwitchDevice = new DeviceConfiguration("stop_button", "Gate stop", DeviceType.EMULATED_SWITCH,
                                                           [state: new SimulatedConnector(1)])
    def openEmulatedSwitchDevice = new DeviceConfiguration("open_button", "Gate open", DeviceType.EMULATED_SWITCH,
                                                           [state: new SimulatedConnector(2)])
    def closeEmulatedSwitchDevice = new DeviceConfiguration("close_button", "Gate close", DeviceType.EMULATED_SWITCH,
                                                           [state: new SimulatedConnector(3)])
    def closedReedSwitchDevice = new DeviceConfiguration("closed_reed_switch", "Closed gate reed switch", DeviceType.REED_SWITCH,
                                                         [state: new SimulatedConnector(4)])
    GatewayConfiguration gateway = gateway([gateDevice, stopEmulatedSwitchDevice, openEmulatedSwitchDevice, closeEmulatedSwitchDevice, closedReedSwitchDevice])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 5
    HomeAssistantCover cover = components.find { it.componentType == HomeAssistantComponentType.COVER } as HomeAssistantCover
    cover.deviceClass == HomeAssistantCover.DeviceClass.GATE
    cover.name == "Test gate device"
    cover.properties.nodeId == gateway.id
    cover.properties.objectId == "myGate"
    cover.stateTopic == expectedStateTopic(gateway.id, gateDevice.id, STATE.toString())
    cover.commandTopic == expectedCommandTopic(gateway.id, gateDevice.id, STATE.toString())
    !cover.retain
    cover.payloadClose == "CLOSE"
    cover.payloadOpen == "OPEN"
    cover.payloadStop == "STOP"
    cover.stateOpen == "OPEN"
    cover.stateClosed == "CLOSED"
    cover.stateOpening == "OPENING"
    cover.stateClosing == "CLOSING"
    cover.stateStopped == null
    cover.uniqueId == gateway.id + "_" + gateDevice.id
    assertHomeAssistantDevice(cover, gateway, gateDevice)
  }

  def "should convert MqGateway device to 6 HA sensors"() {
    given:
    GatewayConfiguration gateway = gateway([])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry)

    then:
    components.size() == 4
    HomeAssistantSensor temperature = components.find { it.properties.objectId.endsWith("TEMPERATURE") } as HomeAssistantSensor
    HomeAssistantSensor freeMemory = components.find { it.properties.objectId.endsWith("MEMORY_FREE") } as HomeAssistantSensor
    HomeAssistantSensor uptime = components.find { it.properties.objectId.endsWith("UPTIME") } as HomeAssistantSensor
    HomeAssistantSensor ipAddress = components.find { it.properties.objectId.endsWith("IP_ADDRESS") } as HomeAssistantSensor

    components.each { component ->
      HomeAssistantSensor sensorComponent = component as HomeAssistantSensor
      assert sensorComponent.properties.device.firmwareVersion == firmwareVersion
      assert sensorComponent.properties.device.identifiers == [gateway.id]
      assert sensorComponent.properties.device.manufacturer == "Aetas"
      assert sensorComponent.properties.device.model == "UniGateway"
      assert sensorComponent.properties.device.viaDevice == null
      assert sensorComponent.properties.device.name == gateway.name
      assert sensorComponent.componentType == HomeAssistantComponentType.SENSOR
      assert sensorComponent.properties.nodeId == gateway.id
      assert sensorComponent.availabilityTopic == "homie/${gateway.id}/\$state"
      assert sensorComponent.payloadAvailable == "ready"
      assert sensorComponent.payloadNotAvailable == "lost"
    }

    temperature.name == "CPU temperature"
    temperature.stateTopic == expectedStateTopic(gateway.id, gateway.id, TEMPERATURE.toString())
    temperature.unitOfMeasurement == DataUnit.CELSIUS.value
    temperature.properties.objectId == gateway.id + "_CPU_TEMPERATURE"
    temperature.uniqueId == gateway.id + "_" + gateway.id + "_CPU_TEMPERATURE"
    freeMemory.name == "Free memory"
    freeMemory.stateTopic == expectedStateTopic(gateway.id, gateway.id, MEMORY.toString())
    freeMemory.unitOfMeasurement == DataUnit.BYTES.value
    freeMemory.properties.objectId == gateway.id + "_MEMORY_FREE"
    freeMemory.uniqueId == gateway.id + "_" + gateway.id + "_MEMORY_FREE"
    uptime.name == "Uptime"
    uptime.stateTopic == expectedStateTopic(gateway.id, gateway.id, UPTIME.toString())
    uptime.unitOfMeasurement == DataUnit.SECOND.value
    uptime.properties.objectId == gateway.id + "_UPTIME"
    uptime.uniqueId == gateway.id + "_" + gateway.id + "_UPTIME"
    ipAddress.name == "IP address"
    ipAddress.stateTopic == expectedStateTopic(gateway.id, gateway.id, IP_ADDRESS.toString())
    ipAddress.unitOfMeasurement == DataUnit.NONE.value
    ipAddress.properties.objectId == gateway.id + "_IP_ADDRESS"
    ipAddress.uniqueId == gateway.id + "_" + gateway.id + "_IP_ADDRESS"
  }

  def "should convert UniGateway light to HA light"() {
    given:
    def lightDeviceConfig = new DeviceConfiguration("myLight", "Test light", DeviceType.LIGHT, [:], [
      relay: new InternalDeviceConfiguration("light_relay"),
      switch1: new InternalDeviceConfiguration("switch_light_1"),
      switch2: new InternalDeviceConfiguration("switch_light_2"),
    ])
    def relay = new DeviceConfiguration("light_relay", "Light Relay", DeviceType.RELAY,[state: new SimulatedConnector(1)])
    def switch1 = new DeviceConfiguration("switch_light_1", "Switch 1", DeviceType.SWITCH_BUTTON, [state: new SimulatedConnector(2)])
    def switch2 = new DeviceConfiguration("switch_light_2", "Switch 2", DeviceType.SWITCH_BUTTON, [state: new SimulatedConnector(3)])
    GatewayConfiguration gateway = gateway([lightDeviceConfig, relay, switch1, switch2])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    def components = converter.convert(deviceRegistry).findAll { isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 4
    HomeAssistantLight lightComponent = components.find { it.componentType == HomeAssistantComponentType.LIGHT } as HomeAssistantLight
    lightComponent.name == "Test light"
    lightComponent.properties.nodeId == gateway.id
    lightComponent.properties.objectId == "myLight"
    lightComponent.stateTopic == expectedStateTopic(gateway.id, lightDeviceConfig.id, STATE.toString())
    lightComponent.commandTopic == expectedCommandTopic(gateway.id, lightDeviceConfig.id, STATE.toString())
    lightComponent.retain
    lightComponent.payloadOn == "ON"
    lightComponent.payloadOff == "OFF"
    lightComponent.uniqueId == gateway.id + "_" + lightDeviceConfig.id
    assertHomeAssistantDevice(lightComponent, gateway, lightDeviceConfig)
  }

  private void assertHomeAssistantDevice(HomeAssistantComponent haComponent, GatewayConfiguration gateway, DeviceConfiguration deviceConfig) {
    assert haComponent.properties.device.firmwareVersion == firmwareVersion
    assert haComponent.properties.device.identifiers == [gateway.id + "_" + deviceConfig.id]
    assert haComponent.properties.device.manufacturer == "Aetas"
    assert haComponent.properties.device.model == "UniGateway ${deviceConfig.type.name()}"
    assert haComponent.properties.device.viaDevice == gateway.id
    assert haComponent.properties.device.name == deviceConfig.name
  }

  static String expectedStateTopic(String gatewayId, String deviceId, String propertyType) {
    return "homie/${gatewayId}/${deviceId}/${propertyType}"
  }

  static String expectedCommandTopic(String gatewayId, String deviceId, String propertyType) {
    return "homie/${gatewayId}/${deviceId}/${propertyType}/set"
  }

  static boolean isNotFromMqGatewayCore(HomeAssistantComponent component, GatewayConfiguration gateway) {
    !component.properties.objectId.startsWith(gateway.id)
  }
}
