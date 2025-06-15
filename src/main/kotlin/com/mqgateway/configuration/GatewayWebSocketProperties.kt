package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.validation.constraints.NotNull

@ConfigurationProperties("gateway.websocket")
data class GatewayWebSocketProperties
  @ConfigurationInject
  constructor(
    @NotNull val enabled: Boolean,
  )
