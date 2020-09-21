package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("homeassistant")
data class HomeAssistantProperties @ConfigurationInject constructor(
  val enabled: Boolean = true,
  val rootTopic: String = "homeassistant"
)
