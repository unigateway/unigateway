package com.unigateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotNull

@ConfigurationProperties("gateway.websocket")
data class GatewayWebSocketProperties @ConfigurationInject constructor(
  @NotNull val enabled: Boolean
)
