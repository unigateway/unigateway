package com.unigateway.core.gatewayconfig.homeassistant

import static com.unigateway.utils.TestGatewayFactory.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.unigateway.configuration.HomeAssistantProperties
import com.unigateway.core.device.DeviceFactoryProvider
import com.unigateway.core.device.DeviceType
import com.unigateway.core.gatewayconfig.DeviceConfiguration
import com.unigateway.core.gatewayconfig.DeviceRegistryFactory
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.hardware.simulated.SimulatedConnector
import com.unigateway.core.utils.FakeSystemInfoProvider
import com.unigateway.core.utils.TimersScheduler
import com.unigateway.utils.MqttClientFactoryStub
import com.unigateway.utils.TestGatewayFactory
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConfigurerTest extends Specification {

  private MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub()

  TestGatewayFactory testGatewayFactory = new TestGatewayFactory()
  DeviceFactoryProvider deviceFactoryProvider = new DeviceFactoryProvider(testGatewayFactory.ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())
  DeviceRegistryFactory deviceRegistryFactory = new DeviceRegistryFactory(deviceFactoryProvider)

  @Subject
  HomeAssistantConfigurer configurer = new HomeAssistantConfigurer(new HomeAssistantProperties(true, "testRoot"),
                                                                   new HomeAssistantConverter("0.0.999-TEST-ONLY"),
                                                                   new HomeAssistantPublisher(new ObjectMapper()),
                                                                   mqttClientFactory)

  def "should connect to MQTT and disconnect after sending HA configurations when gateway configuration has been changed"() {
    given:
    DeviceConfiguration device = new DeviceConfiguration("switchButton_in_test", "Switch Button", DeviceType.SWITCH_BUTTON,
                                                         [state: new SimulatedConnector(1)], [:])
    GatewayConfiguration gateway = gateway([device])
    def deviceRegistry = deviceRegistryFactory.create(gateway)

    when:
    configurer.sendHomeAssistantConfiguration(deviceRegistry)

    then:
    def mqttClient = mqttClientFactory.mqttClient
    mqttClient.connectionTime.isBefore(mqttClient.disconnectionTime)
    !mqttClient.publishedMessages.isEmpty()
  }
}
