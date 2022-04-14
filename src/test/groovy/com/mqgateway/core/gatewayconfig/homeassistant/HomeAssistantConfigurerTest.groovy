package com.mqgateway.core.gatewayconfig.homeassistant

import static com.mqgateway.utils.TestGatewayFactory.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.HomeAssistantProperties
import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.gatewayconfig.DeviceRegistryFactory
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.hardware.simulated.SimulatedConnector
import com.mqgateway.core.hardware.simulated.SimulatedInputOutputProvider
import com.mqgateway.core.hardware.simulated.SimulatedPlatformConfiguration
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.FakeSystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.utils.MqttClientFactoryStub
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConfigurerTest extends Specification {

  private MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub()

  InputOutputProvider ioProvider = new InputOutputProvider(new SimulatedInputOutputProvider(
    new SimulatedPlatformConfiguration("someValue")), new MySensorsInputOutputProvider())
  DeviceFactoryProvider deviceFactoryProvider = new DeviceFactoryProvider(ioProvider, new TimersScheduler(), new FakeSystemInfoProvider())
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
