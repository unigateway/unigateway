package com.unigateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("gateway")
data class GatewayApplicationProperties @ConfigurationInject
constructor(
  @NotBlank val configPath: String,
  @NotBlank val appVersion: String
)
