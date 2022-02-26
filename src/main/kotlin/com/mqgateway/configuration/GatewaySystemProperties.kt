package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ConfigurationProperties("gateway.system")
data class GatewaySystemProperties @ConfigurationInject constructor(
  @NotBlank val networkAdapter: String,
  @NotNull val platform: String, // TODO should this be string or Enum
  @NotNull val platformConfig: Map<String, Any>,
  @NotNull val mqttHostname: String
) {

  interface PlatformConfiguration // TODO this probably should not be here
}
