package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ConfigurationProperties("gateway.system")
data class GatewaySystemProperties @ConfigurationInject constructor(
  @NotBlank val networkAdapter: String = "eth0",
  @NotNull val platform: SystemPlatform,
  @NotNull val expander: ExpanderConfiguration,
  @NotNull val components: ComponentsConfiguration
) {

  enum class SystemPlatform {
    NANOPI, RASPBERRYPI, SIMULATED
  }

  @ConfigurationProperties("expander")
  data class ExpanderConfiguration @ConfigurationInject constructor(
    val enabled: Boolean = false
  ) {
    fun getMcp23017DefaultPorts(): List<String> {
      return if (enabled) {
        listOf("20", "21", "22", "23", "24", "25", "26", "27")
      } else {
        listOf("20", "21", "22", "23")
      }
    }
  }

  @ConfigurationProperties("components")
  data class ComponentsConfiguration @ConfigurationInject constructor(
    @NotNull val mcp23017: Mcp23017Configuration,
    @NotNull val mySensors: MySensors
  ) {

    @ConfigurationProperties("mcp23017")
    data class Mcp23017Configuration @ConfigurationInject constructor(
      private val expander: ExpanderConfiguration,
      private val ports: List<String>? = null
    ) {

      fun getPorts(): List<String> = ports ?: expander.getMcp23017DefaultPorts()
    }

    @ConfigurationProperties("mysensors")
    data class MySensors @ConfigurationInject constructor(
      val enabled: Boolean = false,
      val serialDevice: String = "/myserial"
    )
  }
}
