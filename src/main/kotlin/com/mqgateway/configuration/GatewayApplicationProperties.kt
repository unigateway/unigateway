package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("gateway")
data class GatewayApplicationProperties @ConfigurationInject
constructor(
  @NotBlank val configPath: String = "gateway.yaml",
  @NotBlank val appVersion: String = "SNAPSHOT"
)
