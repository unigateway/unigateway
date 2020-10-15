package com.mqgateway.core.gatewayconfig.homeassistant

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.HomeAssistantProperties
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.utils.MqttClientFactoryStub
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConfigurerTest extends Specification {

	private MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub()

	@Subject
	HomeAssistantConfigurer configurer = new HomeAssistantConfigurer(new HomeAssistantProperties(true, "testRoot"),
																	 new HomeAssistantConverter(),
																	 new HomeAssistantPublisher(new ObjectMapper()),
																	 mqttClientFactory)

	def "should connect to MQTT and disconnect after sending HA configurations when gateway configuration has been changed"() {
		given:
		DeviceConfig device = new DeviceConfig("switchButton_in_test", "Switch Button", DeviceType.SWITCH_BUTTON, [WireColor.BLUE_WHITE], [:], [:])
		Gateway gateway = gateway([room([point([device])])])

		when:
		configurer.sendHomeAssistantConfiguration(gateway)

		then:
		def mqttClient = mqttClientFactory.mqttClient
		mqttClient.connectionTime.isBefore(mqttClient.disconnectionTime)
		!mqttClient.publishedMessages.isEmpty()
	}
}
