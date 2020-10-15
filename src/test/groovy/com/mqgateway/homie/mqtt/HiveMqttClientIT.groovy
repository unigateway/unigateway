package com.mqgateway.homie.mqtt

import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import java.util.concurrent.TimeUnit
import spock.lang.Specification
import spock.lang.Subject

class HiveMqttClientIT extends Specification {

	@Subject
	HiveMqttClient mqttClient

	void setup() {
		def hiveClient = Mqtt3Client.builder()
			.automaticReconnect().initialDelay(100, TimeUnit.MILLISECONDS).maxDelay(1, TimeUnit.SECONDS).applyAutomaticReconnect()
			.identifier("testMqttClient")
			.serverHost("localhost")
			.serverPort(1883)
			.buildBlocking()
		hiveClient.connectWith().cleanSession(true).send()
		mqttClient = new HiveMqttClient(hiveClient)
	}

	void cleanup() {
		mqttClient.disconnect()
	}

	def "should read message from MQTT topic when retained message has been sent earlier"() {
		given:
		def topic = "test/" + UUID.randomUUID().toString()
		mqttClient.publishSync(new MqttMessage(topic, "test payload", 1, true))

		when:
		def readMessage = mqttClient.read(topic)

		then:
		readMessage == "test payload"
	}

	def "should return null when trying to read message from topic which has not received message"() {
		given:
		def topic = "test/" + UUID.randomUUID().toString()

		when:
		def readMessage = mqttClient.read(topic)

		then:
		readMessage == null
		notThrown()
	}


	def "should return null when trying to read message sent non-retained"() {
		given:
		def topic = "test/" + UUID.randomUUID().toString()
		mqttClient.publishSync(new MqttMessage(topic, "test payload", 1, false))

		when:
		def readMessage = mqttClient.read(topic)

		then:
		readMessage == null
	}
}
