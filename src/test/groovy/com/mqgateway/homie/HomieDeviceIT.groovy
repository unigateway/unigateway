package com.mqgateway.homie

import com.mqgateway.ApplicationKt
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import groovy.yaml.YamlSlurper
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

class HomieDeviceIT extends Specification {

  static Set<MqttMessage> receivedMessages = []
  YamlSlurper slurper = new YamlSlurper()

  static BlockingVariable<Boolean> mqGatewayIsReady = new BlockingVariable<>()

  void setupSpec() {
    MqttClientFactory mqttClientFactory = new HiveMqttClientFactory("localhost")

    MqttClient mqttClient = mqttClientFactory.create("testClient") {} {}
    mqttClient.connect(new MqttMessage("test", "disconnected", 0, false), true)
    mqttClient.subscribeAsync("homie/TestGw1/#") { receivedMessages.add(it) }
    mqttClient.subscribeAsync('homie/TestGw1/$state') { if (it.payload == "ready") mqGatewayIsReady.set(true) }
  }

  def "should publish all devices on MQTT when application is starting"() {
    given:
    def gatewayConfiguration = slurper.parse(HomieDeviceIT.getClassLoader().getResourceAsStream('example.gateway.yaml'))

    when:
    ApplicationKt.main()

    then:
    mqGatewayIsReady.get()
    !receivedMessages.isEmpty()
    gatewayConfiguration.rooms*.points*.devices.flatten().forEach{ Map device ->
      assert receivedMessages.find {it.topic == "homie/TestGw1/${device.id}/\$name" }.payload == device.name
    }
  }
}
