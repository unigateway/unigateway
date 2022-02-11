package com.mqgateway.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantConfigurer
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantConverter
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantPublisher
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.mqtt.MqttClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Factory
@Requires(property = "homeassistant.enabled", value = "true")
internal class HomeAssistantFactory {

  @Singleton
  fun homeAssistantConverter(gatewayApplicationProperties: GatewayApplicationProperties): HomeAssistantConverter {
    return HomeAssistantConverter(gatewayApplicationProperties.appVersion)
  }

  @Singleton
  fun homeAssistantPublisher(objectMapper: ObjectMapper): HomeAssistantPublisher {
    return HomeAssistantPublisher(objectMapper)
  }

  @Singleton
  fun homeAssistantConfigurer(
    properties: HomeAssistantProperties,
    converter: HomeAssistantConverter,
    publisher: HomeAssistantPublisher,
    mqttClientFactory: MqttClientFactory
  ): HomeAssistantConfigurer {

    return HomeAssistantConfigurer(properties, converter, publisher, mqttClientFactory)
  }

  @Singleton
  fun homeAssistantMqttListener(homeAssistantConfigurer: HomeAssistantConfigurer, gatewayConfiguration: GatewayConfiguration): HomieDevice.MqttConnectionListener {
    return object : HomieDevice.MqttConnectionListener {
      override fun onConnected() {
        homeAssistantConfigurer.sendHomeAssistantConfiguration(gatewayConfiguration)
      }

      override fun onDisconnect() {}
    }
  }
}
