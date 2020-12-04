package com.mqgateway.homie.gateway

import static com.mqgateway.homie.HomieProperty.DataType.BOOLEAN
import static com.mqgateway.homie.HomieProperty.DataType.FLOAT
import static com.mqgateway.homie.HomieProperty.DataType.INTEGER
import static com.mqgateway.homie.HomieProperty.DataType.STRING
import static com.mqgateway.homie.HomieProperty.Unit.CELSIUS
import static com.mqgateway.homie.HomieProperty.Unit.NONE

import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.homie.HomieReceiver
import com.mqgateway.homie.HomieReceiverStub
import com.mqgateway.homie.mqtt.MqttMessage
import com.mqgateway.utils.MqttClientFactoryStub
import spock.lang.Specification

class GatewayHomieUpdateListenerTest extends Specification {

	MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub()
	HomieReceiver homieReceiver = new HomieReceiverStub()


	def "should notify homie property to publish MQTT message when value has been updated"() {
		given:
		HomieDevice homieDevice = new HomieDevice(
			mqttClientFactory,
			homieReceiver,
			"deviceId1",
			[
				nodeId1: new HomieNode(
					"deviceId1", "nodeId1", "node1 name", "TestType",
					[
						prop1: new HomieProperty("deviceId1", "nodeId1", "prop1", "property 1", INTEGER, "1:100", true, true, NONE),
						prop2: new HomieProperty("deviceId1", "nodeId1", "prop2", "property 2", FLOAT, null, false, false, CELSIUS),
					]),
				nodeId2: new HomieNode(
					"deviceId1", "nodeId2", "node2 name", "AnotherTestType",
					[
						prop1: new HomieProperty("deviceId1", "nodeId2", "prop1", "property 1", STRING, null, true, false, NONE),
						prop3: new HomieProperty("deviceId1", "nodeId2", "prop3", "property 3", BOOLEAN, null, false, true, NONE),
					]),
			],
			"homieVal", "device 1", ["ext1", "ext2"], "test implementation", "fw name", "fw version", "10.0.0.1", "45:12:4c:cf:2a:4e")
		GatewayHomieUpdateListener updateListener = new GatewayHomieUpdateListener(homieDevice)
		homieDevice.connect()

		when:
		updateListener.valueUpdated("nodeId2", "prop3", "new value 123")

		then:
		mqttClientFactory.mqttClient.publishedMessages.contains(new MqttMessage("homie/deviceId1/nodeId2/prop3", "new value 123", 0, true))
	}

	def "should throw when value of unknown property has been updated"() {
		given:
		HomieDevice homieDevice = new HomieDevice(
			mqttClientFactory,
			homieReceiver,
			"deviceId1",
			[
				nodeId1: new HomieNode(
					"deviceId1", "nodeId1", "node1 name", "TestType",
					[
						prop1: new HomieProperty("deviceId1", "nodeId1", "prop1", "property 1", INTEGER, "1:100", true, true, NONE)
					])
			],
			"homieVal", "device 1", ["ext1", "ext2"], "test implementation", "fw name", "fw version", "10.0.0.1", "45:12:4c:cf:2a:4e")
		GatewayHomieUpdateListener updateListener = new GatewayHomieUpdateListener(homieDevice)
		homieDevice.connect()

		when:
		updateListener.valueUpdated("nodeId1", "prop2", "new value")

		then:
		thrown(UnknownHomiePropertyException)
	}
}
