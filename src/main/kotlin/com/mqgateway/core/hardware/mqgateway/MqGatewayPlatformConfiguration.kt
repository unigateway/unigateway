package com.mqgateway.core.hardware.mqgateway

data class MqGatewayPlatformConfiguration(
  val expander: ExpanderConfiguration,
  val components: ComponentsConfiguration,
  val defaultDebounceMs: Int = 50
) {

  data class ExpanderConfiguration(
    val enabled: Boolean = false
  ) {
    fun getMcp23017DefaultPorts(): List<Int> {
      return if (enabled) {
        listOf("20", "21", "22", "23", "24", "25", "26", "27").map { it.toInt(16) }
      } else {
        listOf("20", "21", "22", "23").map { it.toInt(16) }
      }
    }
  }

  data class ComponentsConfiguration(
    val mcp23017: Mcp23017Configuration
  ) {

    data class Mcp23017Configuration(
      private val expander: ExpanderConfiguration,
      private val ports: List<Int>? = null
    ) {

      fun getPorts(): List<Int> = ports ?: expander.getMcp23017DefaultPorts()
    }
  }
}
