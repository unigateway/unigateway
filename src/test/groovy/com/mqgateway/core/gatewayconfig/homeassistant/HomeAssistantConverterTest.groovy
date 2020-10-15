package com.mqgateway.core.gatewayconfig.homeassistant

import static com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.LAST_PING
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.POSITION
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.POWER
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import static com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.mqgateway.core.gatewayconfig.DataUnit
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.WireColor
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConverterTest extends Specification {

	@Subject
	HomeAssistantConverter converter = new HomeAssistantConverter()


	def "should convert MqGateway relay to HA light when set explicitly in gateway configuration"() {
		given:
		def relayDeviceConfig = new DeviceConfig("myRelay", "Test relay", DeviceType.RELAY, [WireColor.BLUE], ["haComponent":"light"], [:])
		Gateway gateway = gateway([room([point("point name", 2, [relayDeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 1
		HomeAssistantLight lightComponent = components[0] as HomeAssistantLight
		lightComponent.componentType == HomeAssistantComponentType.LIGHT
		lightComponent.properties.name == "Test relay"
		lightComponent.properties.nodeId == "gtwName"
		lightComponent.properties.objectId == "myRelay"
		lightComponent.stateTopic == expectedStateTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		lightComponent.commandTopic == expectedCommandTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		lightComponent.retain
		lightComponent.payloadOn == "ON"
		lightComponent.payloadOff == "OFF"
	}

	def "should convert MqGateway relay to HA switch when haComponent not set explicitly in gateway configuration"() {
		given:
		def relayDeviceConfig = new DeviceConfig("myRelay", "Test relay", DeviceType.RELAY, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [relayDeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 1
		HomeAssistantSwitch switchComponent = components[0] as HomeAssistantSwitch
		switchComponent.componentType == HomeAssistantComponentType.SWITCH
		switchComponent.properties.name == "Test relay"
		switchComponent.properties.nodeId == "gtwName"
		switchComponent.properties.objectId == "myRelay"
		switchComponent.stateTopic == expectedStateTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		switchComponent.commandTopic == expectedCommandTopic(gateway.name, relayDeviceConfig.id, STATE.toString())
		switchComponent.retain
		switchComponent.payloadOn == "ON"
		switchComponent.payloadOff == "OFF"
	}

	def "should convert MqGateway SWITCH_BUTTON to 4 HA triggers"() {
		given:
		def switchButtonDeviceConfig = new DeviceConfig("mySwitchButton", "Test button", DeviceType.SWITCH_BUTTON, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [switchButtonDeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 4
		HomeAssistantTrigger pressedTrigger = components.find {it.payload == "PRESSED" } as HomeAssistantTrigger
		HomeAssistantTrigger releasedTrigger = components.find {it.payload == "RELEASED" } as HomeAssistantTrigger
		HomeAssistantTrigger longPressedTrigger = components.find {it.payload == "LONG_PRESSED" } as HomeAssistantTrigger
		HomeAssistantTrigger longReleasedTrigger = components.find {it.payload == "LONG_RELEASED" } as HomeAssistantTrigger

		components.every {component ->
			HomeAssistantTrigger triggerComponent = component as HomeAssistantTrigger
			triggerComponent.componentType == HomeAssistantComponentType.TRIGGER
			triggerComponent.properties.name == "Test button"
			triggerComponent.properties.nodeId == "gtwName"
			triggerComponent.properties.objectId == "mySwitchButton"
			triggerComponent.topic == expectedStateTopic(gateway.name, switchButtonDeviceConfig.id, STATE.toString())
			triggerComponent.subtype == "button"
		}

		pressedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_SHORT_PRESS
		releasedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_SHORT_RELEASE
		longPressedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_LONG_PRESS
		longReleasedTrigger.type == HomeAssistantTrigger.TriggerType.BUTTON_LONG_RELEASE
	}

	def "should convert MqGateway REED_SWITCH to HA binary sensor"() {
		given:
		def reedSwitchDeviceConfig = new DeviceConfig("myReedSwitch", "Test reed switch", DeviceType.REED_SWITCH, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [reedSwitchDeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 1
		HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
		binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
		binarySensorComponent.properties.name == "Test reed switch"
		binarySensorComponent.properties.nodeId == "gtwName"
		binarySensorComponent.properties.objectId == "myReedSwitch"
		binarySensorComponent.stateTopic == expectedStateTopic(gateway.name, reedSwitchDeviceConfig.id, STATE.toString())
		binarySensorComponent.payloadOn == "OPEN"
		binarySensorComponent.payloadOff == "CLOSED"
		binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.OPENING
	}

	def "should convert MqGateway MOTION_DETECTOR to HA binary sensor"() {
		given:
		def motionDetectorDeviceConfig = new DeviceConfig("myMotionDetector", "Test motion detector", DeviceType.MOTION_DETECTOR, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [motionDetectorDeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 1
		HomeAssistantBinarySensor binarySensorComponent = components[0] as HomeAssistantBinarySensor
		binarySensorComponent.componentType == HomeAssistantComponentType.BINARY_SENSOR
		binarySensorComponent.properties.name == "Test motion detector"
		binarySensorComponent.properties.nodeId == "gtwName"
		binarySensorComponent.properties.objectId == "myMotionDetector"
		binarySensorComponent.stateTopic == expectedStateTopic(gateway.name, motionDetectorDeviceConfig.id, STATE.toString())
		binarySensorComponent.payloadOn == "ON"
		binarySensorComponent.payloadOff == "OFF"
		binarySensorComponent.deviceClass == HomeAssistantBinarySensor.DeviceClass.MOTION
	}

	def "should convert MqGateway EMULATED_SWITCH to HA switch"() {
		given:
		def emulatedSwitchDeviceConfig = new DeviceConfig("myEmulatedSwitch", "Test emulated switch", DeviceType.EMULATED_SWITCH, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [emulatedSwitchDeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 1
		HomeAssistantSwitch switchComponent = components[0] as HomeAssistantSwitch
		switchComponent.componentType == HomeAssistantComponentType.SWITCH
		switchComponent.properties.name == "Test emulated switch"
		switchComponent.properties.nodeId == "gtwName"
		switchComponent.properties.objectId == "myEmulatedSwitch"
		switchComponent.stateTopic == expectedStateTopic(gateway.name, emulatedSwitchDeviceConfig.id, STATE.toString())
		switchComponent.commandTopic == expectedCommandTopic(gateway.name, emulatedSwitchDeviceConfig.id, STATE.toString())
		switchComponent.retain == false
		switchComponent.payloadOn == "PRESSED"
		switchComponent.payloadOff == "RELEASED"
	}

	def "should convert MqGateway BME280 to 4 HA sensors"() {
		given:
		def bme280DeviceConfig = new DeviceConfig("myBme280", "Test bme280", DeviceType.BME280, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [bme280DeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 4
		HomeAssistantSensor temperature = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.TEMPERATURE } as HomeAssistantSensor
		HomeAssistantSensor humidity = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.HUMIDITY } as HomeAssistantSensor
		HomeAssistantSensor pressure = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.PRESSURE } as HomeAssistantSensor
		HomeAssistantSensor lastPing = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.TIMESTAMP } as HomeAssistantSensor

		components.every {component ->
			HomeAssistantSensor sensorComponent = component as HomeAssistantSensor
			sensorComponent.componentType == HomeAssistantComponentType.SENSOR
			sensorComponent.properties.name == "Test bme280"
			sensorComponent.properties.nodeId == "gtwName"
			sensorComponent.properties.objectId == "myBme280"
			sensorComponent.availabilityTopic == expectedStateTopic(gateway.name, bme280DeviceConfig.id, STATE.toString())
			sensorComponent.payloadAvailable == "ONLINE"
			sensorComponent.payloadNotAvailable == "OFFLINE"
		}

		temperature.stateTopic == expectedStateTopic(gateway.name, bme280DeviceConfig.id, TEMPERATURE.toString())
		temperature.unitOfMeasurement == DataUnit.CELSIUS.value
		humidity.stateTopic == expectedStateTopic(gateway.name, bme280DeviceConfig.id, HUMIDITY.toString())
		humidity.unitOfMeasurement == DataUnit.PERCENT.value
		pressure.stateTopic == expectedStateTopic(gateway.name, bme280DeviceConfig.id, PRESSURE.toString())
		pressure.unitOfMeasurement == DataUnit.PASCAL.value
		lastPing.stateTopic == expectedStateTopic(gateway.name, bme280DeviceConfig.id, LAST_PING.toString())
		lastPing.unitOfMeasurement == DataUnit.NONE.value
	}

	def "should convert MqGateway DTH22 to 3 HA sensors"() {
		given:
		def dht22DeviceConfig = new DeviceConfig("myDht22", "Test dht22", DeviceType.DHT22, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [dht22DeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 3
		HomeAssistantSensor temperature = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.TEMPERATURE } as HomeAssistantSensor
		HomeAssistantSensor humidity = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.HUMIDITY } as HomeAssistantSensor
		HomeAssistantSensor lastPing = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.TIMESTAMP } as HomeAssistantSensor

		components.every {component ->
			HomeAssistantSensor sensorComponent = component as HomeAssistantSensor
			sensorComponent.componentType == HomeAssistantComponentType.SENSOR
			sensorComponent.properties.name == "Test dht22"
			sensorComponent.properties.nodeId == "gtwName"
			sensorComponent.properties.objectId == "myDht22"
			sensorComponent.availabilityTopic == expectedStateTopic(gateway.name, dht22DeviceConfig.id, STATE.toString())
			sensorComponent.payloadAvailable == "ONLINE"
			sensorComponent.payloadNotAvailable == "OFFLINE"
		}

		temperature.stateTopic == expectedStateTopic(gateway.name, dht22DeviceConfig.id, TEMPERATURE.toString())
		temperature.unitOfMeasurement == DataUnit.CELSIUS.value
		humidity.stateTopic == expectedStateTopic(gateway.name, dht22DeviceConfig.id, HUMIDITY.toString())
		humidity.unitOfMeasurement == DataUnit.PERCENT.value
		lastPing.stateTopic == expectedStateTopic(gateway.name, dht22DeviceConfig.id, LAST_PING.toString())
		lastPing.unitOfMeasurement == DataUnit.NONE.value
	}

	def "should convert MqGateway SCT013 to 2 HA sensors"() {
		given:
		def sct013DeviceConfig = new DeviceConfig("mySct013", "Test SCT013", DeviceType.SCT013, [WireColor.BLUE], [:], [:])
		Gateway gateway = gateway([room([point("point name", 2, [sct013DeviceConfig])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 2
		HomeAssistantSensor power = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.POWER } as HomeAssistantSensor
		HomeAssistantSensor lastPing = components.find {it.deviceClass == HomeAssistantSensor.DeviceClass.TIMESTAMP } as HomeAssistantSensor

		components.every {component ->
			HomeAssistantSensor sensorComponent = component as HomeAssistantSensor
			sensorComponent.componentType == HomeAssistantComponentType.SENSOR
			sensorComponent.properties.name == "Test SCT013"
			sensorComponent.properties.nodeId == "gtwName"
			sensorComponent.properties.objectId == "mySct013"
			sensorComponent.availabilityTopic == expectedStateTopic(gateway.name, sct013DeviceConfig.id, STATE.toString())
			sensorComponent.payloadAvailable == "ONLINE"
			sensorComponent.payloadNotAvailable == "OFFLINE"
		}

		power.stateTopic == expectedStateTopic(gateway.name, sct013DeviceConfig.id, POWER.toString())
		power.unitOfMeasurement == DataUnit.WATT.value
		lastPing.stateTopic == expectedStateTopic(gateway.name, sct013DeviceConfig.id, LAST_PING.toString())
		lastPing.unitOfMeasurement == DataUnit.NONE.value
	}

	def "should convert MqGateway SHUTTER to HA cover"() {
		given:
		def shutterDevice = new DeviceConfig("myShutter", "Test shutter device", DeviceType.SHUTTER, [],
											 [fullOpenTimeMs: "1000", fullCloseTimeMs: "800"],
											 [
												 stopRelay  : new DeviceConfig("stopRelay", "relay1", DeviceType.RELAY, [WireColor.BLUE], [:], [:]),
												 upDownRelay: new DeviceConfig("upDownRelay", "relay2", DeviceType.RELAY, [WireColor.GREEN], [:], [:])
											 ])
		Gateway gateway = gateway([room([point("point name", 3, [shutterDevice])])])

		when:
		def components = converter.convert(gateway)

		then:
		components.size() == 1
		HomeAssistantCover cover = components.first() as HomeAssistantCover
		cover.componentType == HomeAssistantComponentType.COVER
		cover.deviceClass == HomeAssistantCover.DeviceClass.SHUTTER
		cover.properties.name == "Test shutter device"
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
	}



	static String expectedStateTopic(String gatewayName, String deviceId, String propertyType) {
		return "homie/${gatewayName}/${deviceId}/${propertyType}"
	}

	static String expectedCommandTopic(String gatewayName, String deviceId, String propertyType) {
		return "homie/${gatewayName}/${deviceId}/${propertyType}/set"
	}
}
