package com.mqgateway.homie.mqtt

import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.mqgateway.utils.MqttSpecification
import java.util.concurrent.TimeUnit
import spock.lang.Subject
import spock.lang.Timeout

@Timeout(30)
class HiveMqttClientIT extends MqttSpecification {

	@Subject
	HiveMqttClient mqttClient

	void setup() {
    def hiveClient = Mqtt3Client.builder()
			.automaticReconnect().initialDelay(100, TimeUnit.MILLISECONDS).maxDelay(1, TimeUnit.SECONDS).applyAutomaticReconnect()
			.identifier("testMqttClient")
			.serverHost("localhost")
			.serverPort(mosquittoPort())
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

  def "should read all topics with retained messages under given root topic"() {
    given:
    mqttClient.publishSync(new MqttMessage("someRoot/second", "test", 1, true))
    mqttClient.publishSync(new MqttMessage("someRoot/second/third", "test", 1, true))
    mqttClient.publishSync(new MqttMessage("someRoot/second/third/fourth", "test", 1, true))

    when:
    Set<String> topics = mqttClient.findAllSubtopicsWithRetainedMessages("someRoot")

    then:
    topics == ["someRoot/second", "someRoot/second/third", "someRoot/second/third/fourth"].toSet()
  }
}
