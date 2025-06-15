package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("gateway.mqtt")
data class MqttProperties
  @ConfigurationInject
  constructor(
    val enabled: Boolean,
    val hostname: String,
    val port: Int,
    val username: String?,
    val password: String?,
  )
