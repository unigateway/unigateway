package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@ConfigurationProperties("gateway.system")
data class GatewaySystemProperties @ConfigurationInject constructor(
  @NotBlank val networkAdapter: String = "eth0",
  @NotNull val platform: SystemPlatform,
  @NotNull val components: ComponentsConfiguration
) {

  enum class SystemPlatform {
    NANOPI, RASPBERRYPI, SIMULATED
  }

  @ConfigurationProperties("components")
  data class ComponentsConfiguration @ConfigurationInject constructor(
    @NotNull val mcp23017: Mcp23017Configuration,
    @NotNull val serial: Serial
  ) {

    @ConfigurationProperties("mcp23017")
    data class Mcp23017Configuration @ConfigurationInject constructor(
      @NotEmpty val ports: List<String> = listOf("0x20", "0x21", "0x22", "0x23")
    )

    @ConfigurationProperties("serial")
    data class Serial @ConfigurationInject constructor(
      @NotBlank val device: String = "/dev/ttyS1"
    )
  }
}
