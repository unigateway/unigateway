package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ConfigurationProperties("gateway.system")
data class GatewaySystemProperties @ConfigurationInject constructor(
  @NotBlank val networkAdapter: String = "eth0",
  @NotNull val platform: String,
  @NotNull val platformConfig: Map<String, Any>
) {

  interface PlatformConfiguration // TODO this probably should not be here
}
