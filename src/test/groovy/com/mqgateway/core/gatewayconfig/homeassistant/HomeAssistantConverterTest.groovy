package com.mqgateway.core.gatewayconfig.homeassistant


import static com.mqgateway.core.gatewayconfig.DevicePropertyType.IP_ADDRESS
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.MEMORY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.POSITION
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.mqgateway.core.gatewayconfig.DataUnit
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.WireColor
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConverterTest extends Specification {

	String firmwareVersion = "1.2.3-TEST-ONLY"

	@Subject
	HomeAssistantConverter converter = new HomeAssistantConverter(firmwareVersion)


	def "should convert MqGateway relay to HA light when set explicitly in gateway configuration"() {
		given:
		def relayDeviceConfig = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, ["haComponent":"light"], [:])
		GatewayConfiguration gateway = gateway([relayDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantLight lightComponent = components[0] as HomeAssistantLight
		lightComponent.componentType == HomeAssistantComponentType.LIGHT
		lightComponent.name == "Test relay"
		lightComponent.properties.nodeId == "gtwName"
		lightComponent.properties.objectId == "myRelay"
		lightComponent.stateTopic == expectedStateTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		lightComponent.commandTopic == expectedCommandTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		lightComponent.retain
		lightComponent.payloadOn == "ON"
		lightComponent.payloadOff == "OFF"
		lightComponent.uniqueId == gateway.name + "_" + relayDeviceConfig.id
		assertHomeAssistantDevice(lightComponent, gateway, relayDeviceConfig)
	}

	def "should convert MqGateway relay to HA switch when haComponent not set explicitly in gateway configuration"() {
		given:
		def relayDeviceConfig = new DeviceConfiguration("myRelay", "Test relay", DeviceType.RELAY, [:], [:])
		GatewayConfiguration gateway = gateway([relayDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantSwitch switchComponent = components[0] as HomeAssistantSwitch
		switchComponent.componentType == HomeAssistantComponentType.SWITCH
		switchComponent.name == "Test relay"
		switchComponent.properties.nodeId == "gtwName"
		switchComponent.properties.objectId == "myRelay"
		switchComponent.stateTopic == expectedStateTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		switchComponent.commandTopic == expectedCommandTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		switchComponent.retain
		switchComponent.payloadOn == "ON"
		switchComponent.payloadOff == "OFF"
		switchComponent.uniqueId == gateway.name + "_" + relayDeviceConfig.id
		assertHomeAssistantDevice(switchComponent, gateway, relayDeviceConfig)
	}

	def "should convert MqGateway SWITCH_BUTTON to 4 HA triggers when haComponent is set to 'device_automation'"() {
		given:
		def switchButtonDeviceConfig = new DeviceConfiguration("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON, [WireColor.BLUE], [haComponent: "device_automation"], [:])
		GatewayConfiguration gateway = gateway([switchButtonDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 4
		HomeAssistantTrigger pressedTrigger = components.find {it.payload == "PRESSED" } as HomeAssistantTrigger
		HomeAssistantTrigger releasedTrigger = components.find {it.payload == "RELEASED" } as HomeAssistantTrigger
		HomeAssistantTrigger longPressedTrigger = components.find {it.payload == "LONG_PRESSED" } as HomeAssistantTrigger
		HomeAssistantTrigger longReleasedTrigger = components.find {it.payload == "LONG_RELEASED" } as HomeAssistantTrigger

		components.each {HomeAssistantComponent component ->
			HomeAssistantTrigger triggerComponent = component as HomeAssistantTrigger
			assertHomeAssistantDevice(component, gateway, switchButtonDeviceConfig)
			assert triggerComponent.componentType == HomeAssistantComponentType.TRIGGER
			assert triggerComponent.properties.nodeId == "gtwName"
			assert triggerComponent.topic == expectedStateTopic(gateway.name, switchButtonDeviceConfig.id, STATE.toString())
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
		def switchButtonDeviceConfig = new DeviceConfiguration("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON, [haComponent: "sensor"], [:])
		GatewayConfiguration gateway = gateway([switchButtonDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantSensor sensorComponent = components[0] as HomeAssistantSensor
		sensorComponent.componentType == HomeAssistantComponentType.SENSOR
		sensorComponent.name == "Test button"
		sensorComponent.properties.nodeId == "gtwName"
		sensorComponent.properties.objectId == "mySwitchButton"
		sensorComponent.stateTopic == expectedStateTopic(gateway.name, switchButtonDeviceConfig.id, STATE.toString())
		sensorComponent.unitOfMeasurement == null
		sensorComponent.deviceClass == HomeAssistantSensor.DeviceClass.NONE
		sensorComponent.uniqueId == gateway.name + "_" + switchButtonDeviceConfig.id
		assertHomeAssistantDevice(sensorComponent, gateway, switchButtonDeviceConfig)
	}

	def "should convert MqGateway SWITCH_BUTTON to binary sensor when haComponent is not set"() {
		given:
		def switchButtonDeviceConfig = new DeviceConfiguration("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON, [:], [:])
		GatewayConfiguration gateway = gateway([switchButtonDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
		binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
		binarySensorComponent.name == "Test button"
		binarySensorComponent.properties.nodeId == gateway.name
		binarySensorComponent.properties.objectId == switchButtonDeviceConfig.id
		binarySensorComponent.stateTopic == expectedStateTopic(gateway.name, switchButtonDeviceConfig.id, STATE.toString())
		binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.NONE
		binarySensorComponent.uniqueId == "${gateway.name}_${switchButtonDeviceConfig.id}"
		binarySensorComponent.payloadOn == "PRESSED"
		binarySensorComponent.payloadOff == "RELEASED"
		binarySensorComponent.uniqueId == gateway.name + "_" + switchButtonDeviceConfig.id
		assertHomeAssistantDevice(binarySensorComponent, gateway, switchButtonDeviceConfig)
	}

	def "should convert MqGateway REED_SWITCH to HA binary sensor"() {
		given:
		def reedSwitchDeviceConfig = new DeviceConfiguration("myReedSwitch", "Test reed switch", DeviceType.REED_SWITCH, [:], [:])
		GatewayConfiguration gateway = gateway([reedSwitchDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
		binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
		binarySensorComponent.name == "Test reed switch"
		binarySensorComponent.properties.nodeId == "gtwName"
		binarySensorComponent.properties.objectId == "myReedSwitch"
		binarySensorComponent.stateTopic == expectedStateTopic(gateway.name, reedSwitchDeviceConfig.id, STATE.toString())
		binarySensorComponent.payloadOn == "OPEN"
		binarySensorComponent.payloadOff == "CLOSED"
		binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.OPENING
		binarySensorComponent.uniqueId == gateway.name + "_" + reedSwitchDeviceConfig.id
		assertHomeAssistantDevice(binarySensorComponent, gateway, reedSwitchDeviceConfig)
	}

	def "should convert MqGateway MOTION_DETECTOR to HA binary sensor"() {
		given:
		def motionDetectorDeviceConfig = new DeviceConfiguration("myMotionDetector", "Test motion detector", DeviceType.MOTION_DETECTOR, [:], [:])
		GatewayConfiguration gateway = gateway([motionDetectorDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
		binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
		binarySensorComponent.name == "Test motion detector"
		binarySensorComponent.properties.nodeId == "gtwName"
		binarySensorComponent.properties.objectId == "myMotionDetector"
		binarySensorComponent.stateTopic == expectedStateTopic(gateway.name, motionDetectorDeviceConfig.id, STATE.toString())
		binarySensorComponent.payloadOn == "ON"
		binarySensorComponent.payloadOff == "OFF"
		binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.MOTION
		binarySensorComponent.uniqueId == gateway.name + "_" + motionDetectorDeviceConfig.id
		assertHomeAssistantDevice(binarySensorComponent, gateway, motionDetectorDeviceConfig)
	}

	def "should convert MqGateway EMULATED_SWITCH to HA switch"() {
		given:
		def emulatedSwitchDeviceConfig = new DeviceConfiguration("myEmulatedSwitch", "Test emulated switch", DeviceType.EMULATED_SWITCH, [:], [:])
		GatewayConfiguration gateway = gateway([emulatedSwitchDeviceConfig])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantSwitch switchComponent = components[0] as HomeAssistantSwitch
		switchComponent.componentType == HomeAssistantComponentType.SWITCH
		switchComponent.name == "Test emulated switch"
		switchComponent.properties.nodeId == "gtwName"
		switchComponent.properties.objectId == "myEmulatedSwitch"
		switchComponent.stateTopic == expectedStateTopic(gateway.name, emulatedSwitchDeviceConfig.id, STATE.toString())
		switchComponent.commandTopic == expectedCommandTopic(gateway.name, emulatedSwitchDeviceConfig.id, STATE.toString())
		switchComponent.retain == false
		switchComponent.payloadOn == "PRESSED"
		switchComponent.payloadOff == "RELEASED"
		switchComponent.uniqueId == gateway.name + "_" + emulatedSwitchDeviceConfig.id
		assertHomeAssistantDevice(switchComponent, gateway, emulatedSwitchDeviceConfig)
	}

	def "should convert MqGateway SHUTTER to HA cover"() {
		given:
		def shutterDevice = new DeviceConfiguration("myShutter", "Test shutter device", DeviceType.SHUTTER, [],
													[fullOpenTimeMs: "1000", fullCloseTimeMs: "800"],
													[
												 stopRelay  : new DeviceConfiguration("stopRelay", "relay1", DeviceType.RELAY, [:], [:]),
												 upDownRelay: new DeviceConfiguration("upDownRelay", "relay2", DeviceType.RELAY, [:], [:])
											 ])
		GatewayConfiguration gateway = gateway([shutterDevice])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantCover cover = components.first() as HomeAssistantCover
		cover.componentType == HomeAssistantComponentType.COVER
		cover.deviceClass == HomeAssistantCover.DeviceClass.SHUTTER
		cover.name == "Test shutter device"
		cover.properties.nodeId == "gtwName"
		cover.properties.objectId == "myShutter"
		cover.stateTopic == expectedStateTopic(gateway.name, shutterDevice.id, STATE.toString())
		cover.commandTopic == expectedCommandTopic(gateway.name, shutterDevice.id, STATE.toString())
		cover.retain == false
		cover.payloadClose == "CLOSE"
		cover.payloadOpen == "OPEN"
		cover.payloadStop == "STOP"
		cover.positionClosed == 0
		cover.positionOpen == 100
		cover.positionTopic == expectedStateTopic(gateway.name, shutterDevice.id, POSITION.toString())
		cover.setPositionTopic == expectedCommandTopic(gateway.name, shutterDevice.id, POSITION.toString())
		cover.uniqueId == gateway.name + "_" + shutterDevice.id
    cover.stateOpen == "OPEN"
    cover.stateClosed == "CLOSED"
    cover.stateOpening == "OPENING"
    cover.stateClosing == "CLOSING"
    cover.stateStopped == null
		assertHomeAssistantDevice(cover, gateway, shutterDevice)
	}

	def "should convert MqGateway GATE to HA cover"() {
		given:
		def gateDevice = new DeviceConfiguration("myGate", "Test gate device", DeviceType.GATE, [], [:],
												 [
												 stopButton  : new DeviceConfiguration("stopButton", "es1", DeviceType.EMULATED_SWITCH, [WireColor.BLUE_WHITE], [:], [:]),
												 openButton  : new DeviceConfiguration("openButton", "es3", DeviceType.EMULATED_SWITCH, [WireColor.BLUE], [:], [:]),
												 closeButton  : new DeviceConfiguration("closeButton", "es3", DeviceType.EMULATED_SWITCH, [WireColor.GREEN_WHITE], [:], [:]),
												 closedReedSwitch: new DeviceConfiguration("closedReedSwitch", "reedSwitch1", DeviceType.REED_SWITCH, [WireColor.GREEN], [:], [:])
											 ])
		GatewayConfiguration gateway = gateway([gateDevice])

		when:
		def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

		then:
		components.size() == 1
		HomeAssistantCover cover = components.first() as HomeAssistantCover
		cover.componentType == HomeAssistantComponentType.COVER
		cover.deviceClass == HomeAssistantCover.DeviceClass.GARAGE
		cover.name == "Test gate device"
		cover.properties.nodeId == "gtwName"
		cover.properties.objectId == "myGate"
		cover.stateTopic == expectedStateTopic(gateway.name, gateDevice.id, STATE.toString())
		cover.commandTopic == expectedCommandTopic(gateway.name, gateDevice.id, STATE.toString())
		cover.retain == false
		cover.payloadClose == "CLOSE"
		cover.payloadOpen == "OPEN"
		cover.payloadStop == "STOP"
		cover.stateOpen == "OPEN"
		cover.stateClosed == "CLOSED"
    cover.stateOpening == "OPENING"
    cover.stateClosing == "CLOSING"
    cover.stateStopped == null
		cover.uniqueId == gateway.name + "_" + gateDevice.id
		assertHomeAssistantDevice(cover, gateway, gateDevice)
	}

	def "should convert MqGateway device to 6 HA sensors"() {
		given:
		GatewayConfiguration gateway = gateway([])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 4
		HomeAssistantSensor temperature = components.find {it.properties.objectId.endsWith("TEMPERATURE") } as HomeAssistantSensor
		HomeAssistantSensor freeMemory = components.find {it.properties.objectId.endsWith("MEMORY_FREE") } as HomeAssistantSensor
		HomeAssistantSensor uptime = components.find {it.properties.objectId.endsWith("UPTIME") } as HomeAssistantSensor
		HomeAssistantSensor ipAddress = components.find {it.properties.objectId.endsWith("IP_ADDRESS") } as HomeAssistantSensor

		components.each {component ->
			HomeAssistantSensor sensorComponent = component as HomeAssistantSensor
			assert sensorComponent.properties.device.firmwareVersion == firmwareVersion
			assert sensorComponent.properties.device.identifiers == [gateway.name]
			assert sensorComponent.properties.device.manufacturer == "Aetas"
			assert sensorComponent.properties.device.model == "MqGateway"
			assert sensorComponent.properties.device.viaDevice == null
			assert sensorComponent.properties.device.name == gateway.name
			assert sensorComponent.componentType == HomeAssistantComponentType.SENSOR
			assert sensorComponent.properties.nodeId == gateway.name
			assert sensorComponent.availabilityTopic == "homie/${gateway.name}/\$state"
			assert sensorComponent.payloadAvailable == "ready"
			assert sensorComponent.payloadNotAvailable == "lost"
		}

    temperature.name == "CPU temperature"
		temperature.stateTopic == expectedStateTopic(gateway.name, gateway.name, TEMPERATURE.toString())
		temperature.unitOfMeasurement == DataUnit.CELSIUS.value
		temperature.properties.objectId == gateway.name + "_CPU_TEMPERATURE"
		temperature.uniqueId == gateway.name + "_" + gateway.name + "_CPU_TEMPERATURE"
    freeMemory.name == "Free memory"
		freeMemory.stateTopic == expectedStateTopic(gateway.name, gateway.name, MEMORY.toString())
		freeMemory.unitOfMeasurement == DataUnit.BYTES.value
		freeMemory.properties.objectId == gateway.name + "_MEMORY_FREE"
		freeMemory.uniqueId == gateway.name + "_" + gateway.name + "_MEMORY_FREE"
    uptime.name == "Uptime"
		uptime.stateTopic == expectedStateTopic(gateway.name, gateway.name, UPTIME.toString())
		uptime.unitOfMeasurement == DataUnit.SECOND.value
		uptime.properties.objectId == gateway.name + "_UPTIME"
		uptime.uniqueId == gateway.name + "_" + gateway.name + "_UPTIME"
    ipAddress.name == "IP address"
		ipAddress.stateTopic == expectedStateTopic(gateway.name, gateway.name, IP_ADDRESS.toString())
		ipAddress.unitOfMeasurement == DataUnit.NONE.value
		ipAddress.properties.objectId == gateway.name + "_IP_ADDRESS"
		ipAddress.uniqueId == gateway.name + "_" + gateway.name + "_IP_ADDRESS"
	}

  def "should convert MqGateway REFERENCE device to HA device"() {
    given:
    def reedSwitchDevice = new DeviceConfiguration("reedSwitch1", "Referenced reed switch", DeviceType.REED_SWITCH, [WireColor.BLUE])
    def referenceDevice = new DeviceConfiguration("referenceToReedSwitch", "reference to reed switch", DeviceType.REFERENCE, [], [:], [:], "reedSwitch1")

    GatewayConfiguration gateway = gateway([referenceDevice, reedSwitchDevice])

    when:
    def components = converter.convert(gateway).findAll{ isNotFromMqGatewayCore(it, gateway) }

    then:
    components.size() == 2
    def reedSwitchComponent = components.find { it.uniqueId() == gateway.name + "_" + reedSwitchDevice.id } as HomeAssistantBinarySensor

    reedSwitchComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
    reedSwitchComponent.name == "Referenced reed switch"
    reedSwitchComponent.properties.nodeId == "gtwName"
    reedSwitchComponent.properties.objectId == "reedSwitch1"
    reedSwitchComponent.stateTopic == expectedStateTopic(gateway.name, reedSwitchDevice.id, STATE.toString())
    reedSwitchComponent.payloadOn == "OPEN"
    reedSwitchComponent.payloadOff == "CLOSED"
    reedSwitchComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.OPENING
    reedSwitchComponent.uniqueId == gateway.name + "_" + reedSwitchDevice.id
    assertHomeAssistantDevice(reedSwitchComponent, gateway, reedSwitchDevice)

    def referenceComponent = components.find { it.uniqueId() == gateway.name + "_" + referenceDevice.id } as HomeAssistantBinarySensor
    referenceComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
    referenceComponent.name == "Referenced reed switch"
    referenceComponent.properties.nodeId == "gtwName"
    referenceComponent.properties.objectId == "referenceToReedSwitch"
    referenceComponent.stateTopic == expectedStateTopic(gateway.name, reedSwitchDevice.id, STATE.toString())
    referenceComponent.payloadOn == "OPEN"
    referenceComponent.payloadOff == "CLOSED"
    referenceComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.OPENING
    referenceComponent.uniqueId == gateway.name + "_" + referenceDevice.id
    assertHomeAssistantDevice(referenceComponent, gateway, referenceDevice)


  }

	private void assertHomeAssistantDevice(HomeAssistantComponent haComponent, GatewayConfiguration gateway, DeviceConfiguration deviceConfig) {
		assert haComponent.properties.device.firmwareVersion == firmwareVersion
		assert haComponent.properties.device.identifiers == [gateway.name + "_" + deviceConfig.id]
		assert haComponent.properties.device.manufacturer == "Aetas"
		assert haComponent.properties.device.model == "MqGateway ${deviceConfig.type.name()}"
		assert haComponent.properties.device.viaDevice == gateway.name
		assert haComponent.properties.device.name == deviceConfig.name
	}

	static String expectedStateTopic(String gatewayName, String deviceId, String propertyType) {
		return "homie/${gatewayName}/${deviceId}/${propertyType}"
	}

	static String expectedCommandTopic(String gatewayName, String deviceId, String propertyType) {
		return "homie/${gatewayName}/${deviceId}/${propertyType}/set"
	}

	static boolean isNotFromMqGatewayCore(HomeAssistantComponent component, GatewayConfiguration gateway) {
		!component.properties.objectId.startsWith(gateway.name)
	}
}
