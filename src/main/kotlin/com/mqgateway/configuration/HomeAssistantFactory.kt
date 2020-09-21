package com.mqgateway.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantConfigurer
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantConverter
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantPublisher
import com.mqgateway.homie.mqtt.MqttClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

// TODO maj: check if beans are not created when not homeassistant.enabled
@Factory
@Requires(property = "homeassistant.enabled", value = "true")
internal class HomeAssistantFactory {

  @Singleton
  fun homeAssistantConverter(): HomeAssistantConverter {
    return HomeAssistantConverter()
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
    mqttClientFactory: MqttClientFactory,
    gateway: Gateway
  ): HomeAssistantConfigurer {

    return HomeAssistantConfigurer(properties, converter, publisher, mqttClientFactory, gateway)
  }
}
