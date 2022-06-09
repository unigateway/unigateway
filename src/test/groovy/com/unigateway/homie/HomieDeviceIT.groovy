package com.unigateway.homie

import com.unigateway.homie.mqtt.HiveMqttClientFactory
import com.unigateway.homie.mqtt.MqttClient
import com.unigateway.homie.mqtt.MqttClientFactory
import com.unigateway.homie.mqtt.MqttMessage
import com.unigateway.utils.MqttSpecification
import groovy.yaml.YamlSlurper
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Timeout
import spock.util.concurrent.BlockingVariable

@Timeout(30)
class HomieDeviceIT extends MqttSpecification {

  static Set<MqttMessage> receivedMessages = []
  YamlSlurper slurper = new YamlSlurper()

  static BlockingVariable<Boolean> unigatewayIsReady = new BlockingVariable<>()

  void setupSpec() {
    cleanupMqtt()
    MqttClientFactory mqttClientFactory = new HiveMqttClientFactory("localhost", mosquittoPort())

    MqttClient mqttClient = mqttClientFactory.create("testClient") {} {}
    mqttClient.connect(new MqttMessage("test", "disconnected", 0, false), true)
    mqttClient.subscribeAsync("homie/simulated_gateway/#") { receivedMessages.add(it) }
    mqttClient.subscribeAsync('homie/simulated_gateway/$state') { if (it.payload == "ready") unigatewayIsReady.set(true) }

    mqttClient.publishSync(new MqttMessage("homie/simulated_gateway/nonExistingDevice1", "something", 1, true))
  }

  def "should publish all devices on MQTT and remove old devices when application is starting"() {
    given:
    def gatewayConfiguration = slurper.parse(HomieDeviceIT.getClassLoader().getResourceAsStream('example.gateway.yaml'))

    when:
    EmbeddedServer server = ApplicationContext.run(EmbeddedServer)

    then:
    unigatewayIsReady.get()
    !receivedMessages.isEmpty()
    gatewayConfiguration.devices.forEach { Map device ->
      assert receivedMessages.find { it.topic == "homie/simulated_gateway/${device.id}/\$name" }.payload == device.name
    }
    receivedMessages.contains(new MqttMessage("homie/simulated_gateway/nonExistingDevice1", "", 0, false)) // deleting retained message

    cleanup:
    server.close()
  }
}
