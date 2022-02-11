package com.mqgateway.core.gatewayconfig.homeassistant

import static com.mqgateway.utils.TestGatewayFactory.gateway
import static com.mqgateway.utils.TestGatewayFactory.point
import static com.mqgateway.utils.TestGatewayFactory.room

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.configuration.HomeAssistantProperties
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.utils.MqttClientFactoryStub
import spock.lang.Specification
import spock.lang.Subject

class HomeAssistantConfigurerTest extends Specification {

	private MqttClientFactoryStub mqttClientFactory = new MqttClientFactoryStub()

	@Subject
	HomeAssistantConfigurer configurer = new HomeAssistantConfigurer(new HomeAssistantProperties(true, "testRoot"),
																	 new HomeAssistantConverter("0.0.999-TEST-ONLY"),
																	 new HomeAssistantPublisher(new ObjectMapper()),
																	 mqttClientFactory)

	def "should connect to MQTT and disconnect after sending HA configurations when gateway configuration has been changed"() {
		given:
		DeviceConfiguration device = new DeviceConfiguration("switchButton_in_test", "Switch Button", DeviceType.SWITCH_BUTTON, [WireColor.BLUE_WHITE], [:], [:])
		GatewayConfiguration gateway = gateway([room([point([device])])])

		when:
		configurer.sendHomeAssistantConfiguration(gateway)

		then:
		def mqttClient = mqttClientFactory.mqttClient
		mqttClient.connectionTime.isBefore(mqttClient.disconnectionTime)
		!mqttClient.publishedMessages.isEmpty()
	}
}
