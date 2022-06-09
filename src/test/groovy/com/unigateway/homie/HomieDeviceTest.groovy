package com.unigateway.homie

import static com.unigateway.homie.HomieProperty.DataType.BOOLEAN
import static com.unigateway.homie.HomieProperty.DataType.FLOAT
import static com.unigateway.homie.HomieProperty.DataType.INTEGER
import static com.unigateway.homie.HomieProperty.DataType.STRING
import static com.unigateway.homie.HomieProperty.Unit.CELSIUS
import static com.unigateway.homie.HomieProperty.Unit.NONE

import com.unigateway.homie.mqtt.MqttMessage
import com.unigateway.utils.MqttClientFactoryStub
import com.unigateway.homie.mqtt.MqttMessage
import com.unigateway.utils.MqttClientFactoryStub
import spock.lang.Specification
import spock.lang.Subject

class HomieDeviceTest extends Specification {

	@Subject
	MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub()
	HomieReceiver homieReceiver = new HomieReceiverStub()

	def "should send homie device configuration after connection to mqtt"() {
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

		when:
		homieDevice.connect()

		then:
		def baseTopic = "homie/deviceId1"
		def qos = 1
		Set<MqttMessage> expectedMessages = [
			new MqttMessage("$baseTopic/\$state", "init", qos, true),
			new MqttMessage("$baseTopic/\$homie", "homieVal", qos, true),
			new MqttMessage("$baseTopic/\$name", "device 1", qos, true),
			new MqttMessage("$baseTopic/\$extensions", "ext1, ext2", qos, true),
			new MqttMessage("$baseTopic/\$nodes", "nodeId1, nodeId2", qos, true),
			new MqttMessage("$baseTopic/\$implementation", "test implementation", qos, true),
			new MqttMessage("$baseTopic/\$fw/name", "fw name", qos, true),
			new MqttMessage("$baseTopic/\$fw/version", "fw version", qos, true),
			new MqttMessage("$baseTopic/\$localip", "10.0.0.1", qos, true),
			new MqttMessage("$baseTopic/\$mac", "45:12:4c:cf:2a:4e", qos, true),
			new MqttMessage("$baseTopic/\$state", "ready", qos, true),

			new MqttMessage("$baseTopic/nodeId1/\$name", "node1 name", qos, true),
			new MqttMessage("$baseTopic/nodeId1/\$type", "TestType", qos, true),
			new MqttMessage("$baseTopic/nodeId1/\$properties", "prop1, prop2", qos, true),

			new MqttMessage("$baseTopic/nodeId1/prop1/\$name", "property 1", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop1/\$settable", "true", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop1/\$retained", "true", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop1/\$datatype", "integer", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop1/\$format", "1:100", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop2/\$name", "property 2", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop2/\$settable", "false", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop2/\$retained", "false", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop2/\$datatype", "float", qos, true),
			new MqttMessage("$baseTopic/nodeId1/prop2/\$unit", "°C", qos, true),

			new MqttMessage("$baseTopic/nodeId2/\$name", "node2 name", qos, true),
			new MqttMessage("$baseTopic/nodeId2/\$type", "AnotherTestType", qos, true),
			new MqttMessage("$baseTopic/nodeId2/\$properties", "prop1, prop3", qos, true),

			new MqttMessage("$baseTopic/nodeId2/prop1/\$name", "property 1", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop1/\$settable", "true", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop1/\$retained", "false", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop1/\$datatype", "string", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop3/\$name", "property 3", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop3/\$settable", "false", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop3/\$retained", "true", qos, true),
			new MqttMessage("$baseTopic/nodeId2/prop3/\$datatype", "boolean", qos, true),

			new MqttMessage("$baseTopic/\$state", "ready", qos, true)

		].toSet()
		mqttClientFactory.mqttClient.publishedMessages.minus(expectedMessages).isEmpty()
		expectedMessages.minus(mqttClientFactory.mqttClient.publishedMessages).isEmpty()
	}

	def "should subscribe to MQTT topic for settable properties"() {
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

		when:
		homieDevice.connect()

		then:
		mqttClientFactory.mqttClient.subscriptions.keySet() == ["homie/deviceId1/nodeId1/prop1/set", "homie/deviceId1/nodeId2/prop1/set"].toSet()
	}

  def "should remove old devices configuration after connecting to MQTT"() {
    MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub(true)
    mqttClientFactory.create()
    mqttClientFactory.mqttClient.connect(new MqttMessage("", "", 0, false), false)
    mqttClientFactory.mqttClient.publishSync(new MqttMessage('homie/deviceId1/nodeId1/$name', "someName", 1, true))
    HomieDevice homieDevice = new HomieDevice(
      mqttClientFactory,
      homieReceiver,
      "deviceId1",
      [
        nodeId1: new HomieNode(
          "deviceId1", "nodeId1", "node1 name", "TestType",
          [
            prop1: new HomieProperty("deviceId1", "nodeId1", "prop1", "property 1", INTEGER, "1:100", true, true, NONE)
          ]),
      ],
      "homieVal", "device 1", ["ext1", "ext2"], "test implementation", "fw name", "fw version", "10.0.0.1", "45:12:4c:cf:2a:4e")

    when:
    homieDevice.connect()

    then:
    mqttClientFactory.mqttClient.publishedMessages.contains(new MqttMessage('homie/deviceId1/nodeId1/$name', "", 0, false))
  }

}


