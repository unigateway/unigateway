package com.mqgateway.core.gatewayconfig.homeassistant

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.homie.mqtt.MqttMessage
import com.mqgateway.utils.MqttClientStub
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantPublisherTest extends Specification {

	@Subject
	HomeAssistantPublisher publisher = new HomeAssistantPublisher(new ObjectMapper())

	MqttClientStub mqttClientStub = new MqttClientStub()

	HomeAssistantDevice haDevice = new HomeAssistantDevice([], "manufacturerTestName", "modelTestName", "deviceTestName", "fwTestVersion", "viaDeviceTestValue")

	void setup() {
		mqttClientStub.connect(new MqttMessage("", "", 1, true), true)
	}

	def "should publish all components configurations to the proper MQTT topic"() {
		given:
		def component1 = new HomeAssistantLight(
      new HomeAssistantComponentBasicProperties(haDevice, "someNodeId1", "someObjectId1", "test1Name"),
      "someStateTopic1", "someCommandTopic1", true, "ONtest1", "OFFtest1")
		def component2 = new HomeAssistantLight(
      new HomeAssistantComponentBasicProperties(haDevice, "someNodeId2", "someObjectId2", "test2Name"),
      "someStateTopic2", "someCommandTopic2", false, "ONtest2", "OFFtest2")
		def component3 = new HomeAssistantLight(
      new HomeAssistantComponentBasicProperties(haDevice, "someNodeId3", "someObjectId3", ""),
      "someStateTopic3", "someCommandTopic3", false, "ONtest3", "OFFtest3")
		def component4 = new HomeAssistantSelect(
      new HomeAssistantComponentBasicProperties(haDevice, "someNodeId4", "someObjectId4", "Mode"),
      "someStateTopic4", "someCommandTopic4", ["CONTINUOUS", "INTERVAL"], true)

		when:
		publisher.publish(mqttClientStub, "testRoot", [component1, component2, component3, component4])

		then:
		def publishedMessages = mqttClientStub.getPublishedMessages()
		publishedMessages[0].topic == "testRoot/light/someNodeId1/someObjectId1/config"
		publishedMessages[1].topic == "testRoot/light/someNodeId2/someObjectId2/config"
		publishedMessages[2].topic == "testRoot/light/someNodeId3/someObjectId3/config"
		publishedMessages[3].topic == "testRoot/select/someNodeId4/someObjectId4/config"
		assertJsonEqual(component1, new JsonSlurper().parseText(publishedMessages[0].payload) as Map)
		assertJsonEqual(component2, new JsonSlurper().parseText(publishedMessages[1].payload) as Map)
		assertJsonEqual(component3, new JsonSlurper().parseText(publishedMessages[2].payload) as Map)
		assertJsonEqual(component4, new JsonSlurper().parseText(publishedMessages[3].payload) as Map)

		publishedMessages.every { it.retain }
	}

	static void assertJsonEqual(HomeAssistantSelect select, Map jsonSelect) {
		assert select.stateTopic == jsonSelect.state_topic
		assert select.commandTopic == jsonSelect.command_topic
		assert select.options == jsonSelect.options
		assert select.retain == jsonSelect.retain
		assert select.properties.name == jsonSelect.name
		assert select.properties.device.identifiers == jsonSelect.device.identifiers
		assert select.properties.device.name == jsonSelect.device.name
		assert select.properties.device.model == jsonSelect.device.model
		assert select.properties.device.firmwareVersion == jsonSelect.device.sw_version
		assert select.properties.device.manufacturer == jsonSelect.device.manufacturer
		assert select.properties.device.viaDevice == jsonSelect.device.via_device
	}

	static void assertJsonEqual(HomeAssistantLight light, Map jsonLight) {
		assert light.stateTopic == jsonLight.state_topic
		assert light.commandTopic == jsonLight.command_topic
		assert light.retain == jsonLight.retain
		assert light.payloadOn == jsonLight.payload_on
		assert light.payloadOff == jsonLight.payload_off
		assert light.properties.name == jsonLight.name
		assert light.properties.device.identifiers == jsonLight.device.identifiers
		assert light.properties.device.name == jsonLight.device.name
		assert light.properties.device.model == jsonLight.device.model
		assert light.properties.device.firmwareVersion == jsonLight.device.sw_version
		assert light.properties.device.manufacturer == jsonLight.device.manufacturer
		assert light.properties.device.viaDevice == jsonLight.device.via_device
	}

  def "should remove all HA configurations for the given root and node id"() {
    given:
    List<String> haComponentTypes = HomeAssistantComponentType.values().collect { it.value }
    haComponentTypes.forEach {haComponentType ->
      mqttClientStub.publishSync(new MqttMessage("testRoot/$haComponentType/gatewayId/someDevice/config", "something", 1, true))
    }

    when:
    publisher.cleanPublishedConfigurations(mqttClientStub, "testRoot", "gatewayId")

    then:
    List<MqttMessage> expectedCleanMessages = haComponentTypes.collect {type ->
      new MqttMessage("testRoot/$type/gatewayId/someDevice/config", "", 0, false)
    }
    mqttClientStub.publishedMessages.containsAll(expectedCleanMessages)
  }
}
