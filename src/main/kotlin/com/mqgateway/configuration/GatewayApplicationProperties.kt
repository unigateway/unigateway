package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.validation.constraints.NotBlank

@ConfigurationProperties("gateway")
data class GatewayApplicationProperties
  @ConfigurationInject
  constructor(
    @NotBlank val configPath: String,
    @NotBlank val appVersion: String,
  )
