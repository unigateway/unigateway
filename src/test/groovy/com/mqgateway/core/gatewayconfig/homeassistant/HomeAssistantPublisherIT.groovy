package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.mqgateway.homie.mqtt.HiveMqttClientIT
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import com.mqgateway.utils.MqttSpecification
import groovy.json.JsonSlurper
import groovy.yaml.YamlSlurper
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Timeout
import spock.util.concurrent.BlockingVariable

@MicronautTest
class HomeAssistantPublisherIT extends MqttSpecification {

  @Inject
  EmbeddedApplication application

  YamlSlurper yamlSlurper = new YamlSlurper()
  JsonSlurper jsonSlurper = new JsonSlurper()

  static BlockingVariable<Boolean> mqGatewayIsReady = new BlockingVariable<>(10)
  static Set<MqttMessage> receivedMessages = []
  static MqttClient mqttClient


  void setupSpec() {
    MqttClientFactory mqttClientFactory = new HiveMqttClientFactory("localhost", mosquittoPort(), HiveMqttClientIT.MQTT_USERNAME, HiveMqttClientIT.MQTT_PASSWORD)

    mqttClient = mqttClientFactory.create("generalTestClient") {} {}
    mqttClient.connect(new MqttMessage("test", "disconnected", 0, false), true)
    mqttClient.subscribeAsync("homeassistant/#") {
      receivedMessages.add(it)
    }
    mqttClient.subscribeAsync('homie/simulated_gateway/$state') { if (it.payload == "ready") mqGatewayIsReady.set(true) }

    mqttClient.publishSync(new MqttMessage("homie/simulated_gateway/nonExistingDevice1", "something", 1, true))
  }

  void cleanupSpec() {
    mqttClient.disconnect()
  }

	@Timeout(30)
	void 'publish devices for HomeAssistant MQTT discovery on start'() {
    expect:
    def gatewayConfiguration = yamlSlurper.parse(HomeAssistantPublisherIT.getClassLoader().getResourceAsStream('example.gateway.yaml'))

    mqGatewayIsReady.get()
    !receivedMessages.isEmpty()

    gatewayConfiguration.devices.forEach { Map device ->
      String configPayload = receivedMessages.find {
        it.topic ==~ /homeassistant\/[a-zA-Z_-]*\/simulated_gateway\/${device.id}\/config/
      }.payload
      Map parsedConfig = jsonSlurper.parseText(configPayload) as Map
      assert parsedConfig.state_topic == "homie/simulated_gateway/${device.id}/state"
      assert parsedConfig.device.manufacturer == "Aetas"
    }
	}
}
