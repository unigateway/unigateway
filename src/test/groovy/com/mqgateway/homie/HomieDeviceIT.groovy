package com.mqgateway.homie

import com.mqgateway.ApplicationKt
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import com.mqgateway.utils.MqttSpecification
import groovy.yaml.YamlSlurper
import spock.lang.Timeout
import spock.util.concurrent.BlockingVariable

@Timeout(30)
class HomieDeviceIT extends MqttSpecification {

  static Set<MqttMessage> receivedMessages = []
  YamlSlurper slurper = new YamlSlurper()

  static BlockingVariable<Boolean> mqGatewayIsReady = new BlockingVariable<>()

  void setupSpec() {
    MqttClientFactory mqttClientFactory = new HiveMqttClientFactory("localhost", mosquittoPort())

    MqttClient mqttClient = mqttClientFactory.create("testClient") {} {}
    mqttClient.connect(new MqttMessage("test", "disconnected", 0, false), true)
    mqttClient.subscribeAsync("homie/TestGw1/#") { receivedMessages.add(it) }
    mqttClient.subscribeAsync('homie/TestGw1/$state') { if (it.payload == "ready") mqGatewayIsReady.set(true) }

    mqttClient.publishSync(new MqttMessage("homie/TestGw1/nonExistingDevice1", "something", 1, true))
  }

  def "should publish all devices on MQTT and remove old devices when application is starting"() {
    given:
    def gatewayConfiguration = slurper.parse(HomieDeviceIT.getClassLoader().getResourceAsStream('example.gateway.yaml'))

    when:
    ApplicationKt.main()

    then:
    mqGatewayIsReady.get()
    !receivedMessages.isEmpty()
    gatewayConfiguration.rooms*.points*.devices.flatten().forEach { Map device ->
      assert receivedMessages.find {it.topic == "homie/TestGw1/${device.id}/\$name" }.payload == device.name
    }
    receivedMessages.contains(new MqttMessage("homie/TestGw1/nonExistingDevice1", "", 0, false))
  }
}
