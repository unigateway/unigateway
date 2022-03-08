package com.mqgateway.core.hardware.mqgateway

data class MqGatewayPlatformConfiguration(
  val expander: ExpanderConfiguration,
  val components: ComponentsConfiguration
) {

  data class ExpanderConfiguration(
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

  data class ComponentsConfiguration(
    val mcp23017: Mcp23017Configuration
  ) {

    data class Mcp23017Configuration(
      private val expander: ExpanderConfiguration,
      private val ports: List<String>? = null
    ) {

      fun getPorts(): List<String> = ports ?: expander.getMcp23017DefaultPorts()
    }
  }
}
